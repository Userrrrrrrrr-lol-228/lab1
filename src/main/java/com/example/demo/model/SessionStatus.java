package com.example.demo.model;

public enum SessionStatus {
    /** refresh ещё не использован и не отозван */
    ACTIVE,
    /** refresh успешно применён для обновления пары */
    USED,
    /** refresh отозван (например, при повторном предъявлении использованного) */
    REVOKED
}
