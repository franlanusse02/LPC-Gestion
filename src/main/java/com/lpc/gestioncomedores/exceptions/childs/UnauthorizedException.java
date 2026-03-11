package com.lpc.gestioncomedores.exceptions.childs;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
