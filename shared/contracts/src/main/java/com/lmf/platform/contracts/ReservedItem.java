package com.lmf.platform.contracts;

import java.util.UUID;

public record ReservedItem(

        UUID productId,

        Integer quantity) {
}
