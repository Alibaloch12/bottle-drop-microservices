package the.bottle.necks.bottle_drop_point_service.web;

public class EmptiesTransferRequest {

    private Long dropPointId;
    private int quantity;

    public EmptiesTransferRequest() {
    }

    public EmptiesTransferRequest(Long dropPointId, int quantity) {
        this.dropPointId = dropPointId;
        this.quantity = quantity;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}