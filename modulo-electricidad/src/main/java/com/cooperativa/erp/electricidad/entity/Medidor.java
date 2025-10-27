package com.cooperativa.erp.electricidad.entity;

import com.cooperativa.erp.core.entity.Suministro; // Import correcto
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data; // <-- AÑADIR ESTA ANOTACIÓN
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "elec_medidores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numero"}, name = "uk_medidor_numero") // Asegura número único
})
@Data // <-- AÑADIR ESTA ANOTACIÓN (Genera getters, setters, toString, equals, hashCode)
@NoArgsConstructor
public class Medidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String numero; // Número de serie/identificador único del medidor

    @NotBlank
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String modelo;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 4) // Ej: 1.0000 para directo, 10.0000 si multiplica x10
    private BigDecimal constanteMultiplicacion = BigDecimal.ONE; // Valor por defecto

    // Relación con Suministro (Un medidor puede estar en UN suministro a la vez)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy para no cargar siempre el suministro
    @JoinColumn(name = "suministro_id") // Clave foránea en la tabla elec_medidores
    private Suministro suministro; // Puede ser null si el medidor está en depósito

    // Podríamos añadir estado: "Instalado", "En Deposito", "Retirado", etc.
    // @Column(length = 20)
    // private String estadoActual;
}



