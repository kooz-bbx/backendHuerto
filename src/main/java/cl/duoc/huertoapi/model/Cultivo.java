package cl.duoc.huertoapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Entidad JPA: tabla "cultivos". Cada cultivo pertenece a un sector (relación muchos a uno).
@Entity
@Table(name = "cultivos")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    // Clave foránea sector_id → sectores.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "fecha_siembra", nullable = false, length = 10)
    private String fechaSiembra;        // dd/MM/yyyy

    @Column(name = "fecha_trasplante", length = 10)
    private String fechaTrasplante;     // vacío si no corresponde

    @Column(name = "fecha_cosecha", nullable = false, length = 10)
    private String fechaCosecha;        // fecha estimada

    @Column(name = "frecuencia_riego_dias", nullable = false)
    private Integer frecuenciaRiegoDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoCultivo estado = EstadoCultivo.PLANIFICADO;

    @Column(length = 200)
    private String observaciones;

    public Cultivo() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }

    public String getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(String fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public String getFechaTrasplante() { return fechaTrasplante; }
    public void setFechaTrasplante(String fechaTrasplante) { this.fechaTrasplante = fechaTrasplante; }

    public String getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(String fechaCosecha) { this.fechaCosecha = fechaCosecha; }

    public Integer getFrecuenciaRiegoDias() { return frecuenciaRiegoDias; }
    public void setFrecuenciaRiegoDias(Integer frecuenciaRiegoDias) { this.frecuenciaRiegoDias = frecuenciaRiegoDias; }

    public EstadoCultivo getEstado() { return estado; }
    public void setEstado(EstadoCultivo estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
