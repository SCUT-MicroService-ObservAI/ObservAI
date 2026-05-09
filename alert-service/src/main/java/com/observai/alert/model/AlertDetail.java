package com.observai.alert.model;

import java.util.List;

public record AlertDetail(
        AlertRecord alert,
        List<AlertStatusHistory> statusHistory
) {
}

