package com.mycompany.reservationsystem.dto;

public class WebUpdateDTO {
    private String type;
    private String code;
    private String message;
    private String phone;
    private String reference;
    private Object data;
    private String timestamp;
    private int pax;
    private String customerName;
    private String link;

    public WebUpdateDTO() {}

    public WebUpdateDTO(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getPax() { return pax; }
    public void setPax(int pax) { this.pax = pax; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
