package com.mycompany.reservationsystem.dto;

import java.time.LocalTime;

public class UpdateSeatedTimeRequest {
    private LocalTime time;

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
