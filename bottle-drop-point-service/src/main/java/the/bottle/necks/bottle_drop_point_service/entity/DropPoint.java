package the.bottle.necks.bottle_drop_point_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DropPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private int currentFillLevel;
    private int maxCapacity;

    public DropPoint() {
    }

    public DropPoint(String location, int maxCapacity) {
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.currentFillLevel = 0;
    }

    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCurrentFillLevel() {
        return currentFillLevel;
    }

    public void setCurrentFillLevel(int currentFillLevel) {
        this.currentFillLevel = currentFillLevel;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean isFull() {
        return currentFillLevel >= maxCapacity;
    }

    public void acceptReturn(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        if (currentFillLevel + quantity > maxCapacity) {
            throw new IllegalArgumentException("Drop point capacity exceeded.");
        }

        currentFillLevel += quantity;
    }

    public void emptyDropPoint() {
        currentFillLevel = 0;
    }
}