package com.cooperativa.erp.electricidad.entity;

import com.cooperativa.erp.core.entity.Suministro; // Importa desde modulo-core
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "elec_medidores") // Prefijo 'elec_' para tablas de este módulo
@Data // Lombok para getters, setters, toString, equals, hashCode
@NoArgsConstructor
public class Medidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String numeroSerie;

    private String marca;
    private String modelo;

    @NotNull
    private Integer fases = 1; // Monofásico por defecto

    // Podríamos tener una relación OneToOne o ManyToOne aquí.
    // Si un medidor SIEMPRE pertenece a UN suministro, es OneToOne.
    // Si un medidor puede ser REUTILIZADO (raro), sería ManyToOne.
    // Empecemos con OneToOne. Si un suministro cambia de medidor, se DESASOCIA el viejo y se ASOCIA el nuevo.
    @OneToOne(fetch = FetchType.LAZY) // Lazy para no cargarlo siempre
    @JoinColumn(name = "suministro_id", unique = true) // Clave foránea en esta tabla
    private Suministro suministro;
    // ¡Ojo! Suministro viene de modulo-core. ¡La magia del multi-módulo!

    // Otros campos específicos del medidor...
    private BigDecimal constanteMultiplicacion = BigDecimal.ONE; // KM

    // Constructor útil
    public Medidor(String numeroSerie, String marca, String modelo) {
        this.numeroSerie = numeroSerie;
        this.marca = marca;
        this.modelo = modelo;
    }
}

