package com.mycompany.reservationsystem.dto;

import java.util.List;

public class TableStatsDTO {
    public List<ManageTablesDTO> tables;
    public long total;
    public long free;
    public long busy;
}