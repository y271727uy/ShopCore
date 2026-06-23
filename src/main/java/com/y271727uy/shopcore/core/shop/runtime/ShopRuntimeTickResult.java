package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.generation.ShopOrderGenerationResult;
import com.y271727uy.shopcore.core.shop.diagnostic.ShopDiagnostic;
import com.y271727uy.shopcore.core.shop.diagnostic.ShopDiagnosticCode;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationResult;
import com.y271727uy.shopcore.core.shop.session.ShopSessionTickResult;

import java.util.Objects;
import java.util.Optional;

public record ShopRuntimeTickResult(
        ShopInstance shop,
        ShopOrderBook orderBook,
        ShopOperationResult operationResult,
        Optional<ShopOrderGenerationResult> generationResult,
        ShopSessionTickResult sessionResult,
        ShopDiagnostic diagnostic
) {
    public ShopRuntimeTickResult {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(orderBook, "orderBook");
        Objects.requireNonNull(operationResult, "operationResult");
        generationResult = Objects.requireNonNullElse(generationResult, Optional.empty());
        Objects.requireNonNull(sessionResult, "sessionResult");
        Objects.requireNonNull(diagnostic, "diagnostic");
    }

    public boolean open() {
        return shop.isOpen();
    }

    public static ShopDiagnostic diagnosticFor(ShopOperationResult operationResult, Optional<ShopOrderGenerationResult> generationResult) {
        Objects.requireNonNull(operationResult, "operationResult");
        Objects.requireNonNull(generationResult, "generationResult");
        if (generationResult.isPresent() && generationResult.get().generatedOrder()) {
            return generationResult.get().diagnostic();
        }
        ShopDiagnosticCode code = ShopDiagnosticCode.fromOperation(operationResult);
        if (code == ShopDiagnosticCode.NONE && generationResult.isPresent()) {
            return generationResult.get().diagnostic();
        }
        return ShopDiagnostic.of(code == ShopDiagnosticCode.NONE ? ShopDiagnosticCode.RUNTIME_TICKED : code);
    }
}
