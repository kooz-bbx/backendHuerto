package cl.duoc.huertoapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entidad JPA: Hibernate crea la tabla "sectores" a partir de esta clase.
@Entity
@Table(name = "sectores")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // id autoincremental
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;              // ej: S-01 (no se puede repetir)

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String ubicacion;

    @Enumerated(EnumType.STRING)        // se guarda como texto: "EN_USO"
    @Column(nullable = false, length = 20)
    private EstadoSector estado = EstadoSector.DISPONIBLE;

    @Column(name = "responsable_id", nullable = false)
    private Integer responsableId;      // id del docente o tallerista responsable

    @Column(name = "fecha_creacion", length = 10)
    private String fechaCreacion;       // dd/MM/yyyy

    @Column(name = "fecha_actualizacion", length = 10)
    private String fechaActualizacion;  // dd/MM/yyyy

    public Sector() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public EstadoSector getEstado() { return estado; }
    public void setEstado(EstadoSector estado) { this.estado = estado; }

    public Integer getResponsableId() { return responsableId; }
    public void setResponsableId(Integer responsableId) { this.responsableId = responsableId; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
