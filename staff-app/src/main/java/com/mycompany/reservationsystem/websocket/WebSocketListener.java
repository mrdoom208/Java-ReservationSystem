/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.websocket;

import com.mycompany.reservationsystem.dto.WebUpdateDTO;

/**
 *
 * @author formentera
 */
public interface WebSocketListener {
    void onMessage(WebUpdateDTO dto);

    default void onConnecting() {}

    default void onConnected() {}

    default void onConnectionError(String message) {}
}
