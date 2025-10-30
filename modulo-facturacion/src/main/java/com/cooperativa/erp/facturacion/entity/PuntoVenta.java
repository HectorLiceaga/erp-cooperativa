package com.cooperativa.erp.facturacion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "fac_puntos_venta", uniqueConstraints = {
        @UniqueConstraint(name = "uk_puntoventa_numero", columnNames = {"numero"})
})
@Data // Asegura getters, setters, etc.
@NoArgsConstructor
@AllArgsConstructor
public class PuntoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive // El número de PV debe ser positivo
    @Column(nullable = false, unique = true)
    private Integer numero; // Ej: 1, 2, 3

    @NotBlank
    @Column(nullable = false, length = 100)
    private String descripcion; // Ej: "Administración", "Ventas Mostrador"

    @NotNull
    @Column(nullable = false)
    private Boolean habilitado = true; // Para poder deshabilitar un PV

    @NotNull
    @Column(nullable = false)
    private Boolean facturacionElectronica = false; // Indica si usa CAE de AFIP

    // TODO: Mejorar esta parte. Necesitamos guardar el último número POR TIPO de comprobante.
    // Una opción es usar un Map, pero JPA/Hibernate tiene limitaciones con Maps complejos.
    // Otra opción es una entidad separada: PuntoVentaUltimoNumero(puntoVentaId, tipoComprobanteId, ultimoNumero)
    // Por ahora, un placeholder simple:
    @Column(nullable = false)
    private Long ultimoNumeroEmitido = 0L; // Simplificación MUY grande

    // Ejemplo de cómo podría ser con un Map (requiere configuración adicional o ser JSON/String)
    // @ElementCollection
    // @CollectionTable(name = "fac_pv_ultimos_numeros", joinColumns = @JoinColumn(name = "punto_venta_id"))
    // @MapKeyJoinColumn(name = "comprobante_tipo_id")
    // @Column(name = "ultimo_numero")
    // private Map<ComprobanteTipo, Long> ultimosNumerosPorTipo = new HashMap<>();

}

