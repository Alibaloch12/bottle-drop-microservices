package the.bottle.necks.bottle_drop_point_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BottleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    private LocalDateTime returnTime;

    @ManyToOne
    @JoinColumn(name = "drop_point_id")
    private DropPoint dropPoint;

    public BottleReturn() {
    }

    public BottleReturn(int quantity, LocalDateTime returnTime) {
        this.quantity = quantity;
        this.returnTime = returnTime;
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public DropPoint getDropPoint() {
        return dropPoint;
    }

    public void setDropPoint(DropPoint dropPoint) {
        this.dropPoint = dropPoint;
    }
}