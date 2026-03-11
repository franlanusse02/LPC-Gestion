package com.lpc.gestioncomedores.services.facturaproveedor;


import com.lpc.gestioncomedores.dtos.facturaproveedor.*;
import com.lpc.gestioncomedores.exceptions.childs.NotFoundException;
import com.lpc.gestioncomedores.models.admin.Banco;
import com.lpc.gestioncomedores.models.comedores.Comedor;
import com.lpc.gestioncomedores.models.enums.EstadoFacturaProveedor;
import com.lpc.gestioncomedores.models.enums.Sentido;
import com.lpc.gestioncomedores.models.movimientos.childs.MovimientoPagoProveedor;
import com.lpc.gestioncomedores.models.proveedores.FacturaProveedor;
import com.lpc.gestioncomedores.models.proveedores.Proveedor;
import com.lpc.gestioncomedores.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacturaProveedorService {
    private final FacturaProveedorRepository facturaRepo;
    private final BancoRepository bancoRepo;
    private final ComedorRepository comedorRepo;
    private final ProveedorRepository proveedorRepo;
    private final MovimientoPagoProveedorRepository movimientoPagoRepo;

    @Transactional
    public FacturaProveedorResponse create(CreateFacturaProveedorRequest req) {
        Proveedor proveedor = proveedorRepo.findById(req.proveedorId())
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado."));
        Comedor comedor = comedorRepo.findById(req.comedorId())
                .orElseThrow(() -> new NotFoundException("Comedor no encontrado."));

        FacturaProveedor factura = FacturaProveedor.create(
                proveedor,
                comedor,
                req.numeroFactura(),
                req.fechaFactura(),
                req.monto(),
                req.medioPago(),
                req.observaciones()
        );

        factura = facturaRepo.save(factura);
        return toResponse(factura, null);
    }

    @Transactional
    public FacturaProveedorResponse patch(Long facturaId, UpdateFacturaProveedorRequest req) {
        FacturaProveedor factura = getFacturaOrThrow(facturaId);
        assertEditable(factura);

        Proveedor proveedor = req.proveedorId() == null ? null:
                proveedorRepo.findById(req.proveedorId())
                        .orElseThrow(() -> new NotFoundException("Proveedor no encontrado."));

        Comedor comedor = req.comedorId() == null ? null:
                comedorRepo.findById(req.comedorId())
                        .orElseThrow(() -> new NotFoundException("Comedor no encontrado."));

        factura.applyReviewPatch(
                proveedor,
                comedor,
                req.numeroFactura(),
                req.fechaFactura(),
                req.monto(),
                req.medioPago(),
                req.observaciones()
        );

        return toResponse(factura, null);

    }

    @Transactional
    public FacturaProveedorResponse approve(Long facturaId, ApproveFacturaProveedorRequest req) {
        FacturaProveedor factura = getFacturaOrThrow(facturaId);
        if (factura.getEstado() != EstadoFacturaProveedor.PENDIENTE &&
                factura.getEstado() != EstadoFacturaProveedor.REVISAR) {
            throw new IllegalStateException("Solo se puede aprobar una factura en PENDIENTE o REVISAR");
        }

        Banco banco = req.bancoPagadorId() == null ? null:
                bancoRepo.findById(req.bancoPagadorId())
                        .orElseThrow(() -> new NotFoundException("Banco no encontrado."));

        factura.approvePayment(
                req.fechaPagoProvisoria(),
                req.fechaEmision(),
                banco,
                req.observaciones()
        );
        return toResponse(factura, null);
    }


    @Transactional
    public FacturaProveedorResponse pay(Long facturaId, PayFacturaProveedorRequest req) {
        FacturaProveedor factura = getFacturaOrThrow(facturaId);
        if (factura.getEstado() != EstadoFacturaProveedor.EMITIDO) {
            throw new IllegalStateException("Solo se puede pagar una factura con estado EMITIDO");
        }
        if (movimientoPagoRepo.existsByFactura_Id(facturaId)) {
            throw new IllegalStateException("La factura ya tiene un movimiento de pago.");
        }

        factura.markAsPaid(req.fechaPago(), req.observaciones());

        MovimientoPagoProveedor mov = new MovimientoPagoProveedor();

        mov.setFactura(factura);
        mov.setComedor(factura.getComedor());
        mov.setMonto(factura.getMonto());
        mov.setMedioPago(factura.getMedioPago());
        mov.setFechaHora(Instant.now());
        mov.setSentido(Sentido.EGRESO);
        mov.setComentarios(factura.getObservaciones());

        mov = movimientoPagoRepo.save(mov);

        return toResponse(factura, mov.getId());
    }

    @Transactional
    public FacturaProveedorResponse cancel(Long facturaId, String observaciones) {
        FacturaProveedor factura = getFacturaOrThrow(facturaId);
        if (factura.getEstado() == EstadoFacturaProveedor.PAGADO) {
            throw new IllegalStateException("No se puede cancelar una factura ya pagada.");
        }

        factura.cancelPayment(observaciones);

        return toResponse(factura, null);
    }

    public FacturaProveedorResponse getById(Long facturaId) {
        FacturaProveedor factura = getFacturaOrThrow(facturaId);
        return toResponse(factura, null);
    }

    private FacturaProveedor getFacturaOrThrow(Long id) {
        return facturaRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada."));
    }

    private void assertEditable(FacturaProveedor factura) {
        if (factura.getEstado() != EstadoFacturaProveedor.PENDIENTE &&
                factura.getEstado() != EstadoFacturaProveedor.REVISAR) {
            throw new IllegalStateException("Solo se puede editar una factura con estado PENDIENTE o REVISAR.");
        }
    }

    private FacturaProveedorResponse toResponse(FacturaProveedor f, Long pagoIdOverride) {
        Long pagoId = pagoIdOverride != null
                ? pagoIdOverride
                : (f.getPago() != null ? f.getPago().getId() : null);

        return new FacturaProveedorResponse(
                f.getId(),
                f.getComedor().getId(),
                f.getComedor().getName(),
                f.getProveedor().getId(),
                f.getProveedor().getName(),
                f.getProveedor().getTaxId(),
                f.getNumeroFactura(),
                f.getFechaFactura(),
                f.getFechaEmision(),
                f.getMonto(),
                f.getMedioPago(),
                f.getEstado(),
                pagoId,
                f.getFechaPagoProvisoria(),
                f.getFechaPago(),
                f.getObservaciones()
        );
    }
}
