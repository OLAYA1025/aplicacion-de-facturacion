package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.GrupoNotaCategoria;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.NotaPredefinida;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.dto.response.GrupoNotaCategoriaResponseDTO;
import com.facturacion.negocio.enumerated.TipoSeleccionNota;
import com.facturacion.negocio.repository.CategoriaRepository;
import com.facturacion.negocio.repository.GrupoNotaCategoriaRepository;
import com.facturacion.negocio.repository.NotaPredefinidaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class NotasPredefinidasService {

    private final CategoriaRepository categoriaRepository;
    private final GrupoNotaCategoriaRepository grupoNotaCategoriaRepository;
    private final NotaPredefinidaRepository notaPredefinidaRepository;
    private final TenantService tenantService;

    public NotasPredefinidasService(CategoriaRepository categoriaRepository,
                                    GrupoNotaCategoriaRepository grupoNotaCategoriaRepository,
                                    NotaPredefinidaRepository notaPredefinidaRepository,
                                    TenantService tenantService) {
        this.categoriaRepository = categoriaRepository;
        this.grupoNotaCategoriaRepository = grupoNotaCategoriaRepository;
        this.notaPredefinidaRepository = notaPredefinidaRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<GrupoNotaCategoriaResponseDTO> obtenerGruposConNotasPorCategoria(Long categoriaId) {
        validarId(categoriaId, "categoriaId invalido");
        Negocio negocio = tenantService.getNegocioActual();
        List<GrupoNotaCategoria> grupos = grupoNotaCategoriaRepository.findByCategoriaIdWithNotas(categoriaId, negocio);
        grupos.sort(Comparator.comparing(GrupoNotaCategoria::getNombre, String.CASE_INSENSITIVE_ORDER));
        List<GrupoNotaCategoriaResponseDTO> response = new ArrayList<>(grupos.size());
        for (GrupoNotaCategoria grupo : grupos) {
            response.add(GrupoNotaCategoriaResponseDTO.from(grupo));
        }
        return response;
    }

    @Transactional
    public GrupoNotaCategoria crearGrupo(Long categoriaId, String nombre, Boolean obligatorio, TipoSeleccionNota tipoSeleccion) {
        Categoria categoria = obtenerCategoria(categoriaId);
        Negocio negocio = tenantService.getNegocioActual();
        String nombreNormalizado = normalizarNombreGrupo(nombre);
        if (grupoNotaCategoriaRepository.existsByCategoriaIdAndNombreIgnoreCaseAndNegocio(
                categoria.getId(),
                nombreNormalizado,
                negocio
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "el grupo de notas ya existe para esta categoria");
        }

        GrupoNotaCategoria grupo = new GrupoNotaCategoria(nombreNormalizado, obligatorio, tipoSeleccion, categoria);
        grupo.setNegocio(negocio);
        return grupoNotaCategoriaRepository.save(grupo);
    }

    @Transactional
    public GrupoNotaCategoria actualizarGrupo(Long categoriaId,
                                              Long grupoId,
                                              String nombre,
                                              Boolean obligatorio,
                                              TipoSeleccionNota tipoSeleccion) {
        validarId(categoriaId, "categoriaId invalido");
        validarId(grupoId, "grupoId invalido");
        Negocio negocio = tenantService.getNegocioActual();
        GrupoNotaCategoria grupo = grupoNotaCategoriaRepository.findByIdAndCategoriaIdAndNegocio(grupoId, categoriaId, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "grupo de notas no encontrado"));

        String nombreNormalizado = normalizarNombreGrupo(nombre);
        if (grupoNotaCategoriaRepository.existsByCategoriaIdAndNombreIgnoreCaseAndIdNot(
                categoriaId,
                nombreNormalizado,
                grupoId,
                negocio
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ya existe un grupo de notas con ese nombre");
        }

        grupo.actualizar(nombreNormalizado, obligatorio, tipoSeleccion);
        return grupoNotaCategoriaRepository.save(grupo);
    }

    @Transactional
    public void eliminarGrupo(Long categoriaId, Long grupoId) {
        validarId(categoriaId, "categoriaId invalido");
        validarId(grupoId, "grupoId invalido");
        GrupoNotaCategoria grupo = grupoNotaCategoriaRepository.findByIdAndCategoriaIdAndNegocio(
                grupoId,
                categoriaId,
                tenantService.getNegocioActual()
        )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "grupo de notas no encontrado"));
        grupoNotaCategoriaRepository.delete(grupo);
    }

    @Transactional
    public NotaPredefinida crearNota(Long categoriaId, Long grupoId, String descripcion) {
        GrupoNotaCategoria grupo = obtenerGrupo(categoriaId, grupoId);
        Negocio negocio = tenantService.getNegocioActual();
        String descripcionNormalizada = normalizarDescripcionNota(descripcion);
        if (notaPredefinidaRepository.existsByGrupoIdAndDescripcionIgnoreCaseAndNegocio(
                grupoId,
                descripcionNormalizada,
                negocio
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "la nota ya existe en el grupo");
        }

        NotaPredefinida nota = new NotaPredefinida(descripcionNormalizada, grupo);
        nota.setNegocio(negocio);
        return notaPredefinidaRepository.save(nota);
    }

    @Transactional
    public NotaPredefinida actualizarNota(Long categoriaId, Long grupoId, Long notaId, String descripcion) {
        validarId(notaId, "notaId invalido");
        obtenerGrupo(categoriaId, grupoId);
        Negocio negocio = tenantService.getNegocioActual();
        NotaPredefinida nota = notaPredefinidaRepository.findByIdAndGrupoIdAndNegocio(notaId, grupoId, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "nota no encontrada"));

        String descripcionNormalizada = normalizarDescripcionNota(descripcion);
        if (notaPredefinidaRepository.existsByGrupoIdAndDescripcionIgnoreCaseAndIdNotAndNegocio(
                grupoId,
                descripcionNormalizada,
                notaId,
                negocio
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ya existe una nota con esa descripcion");
        }

        nota.setDescripcion(descripcionNormalizada);
        return notaPredefinidaRepository.save(nota);
    }

    @Transactional
    public void eliminarNota(Long categoriaId, Long grupoId, Long notaId) {
        validarId(notaId, "notaId invalido");
        obtenerGrupo(categoriaId, grupoId);
        NotaPredefinida nota = notaPredefinidaRepository.findByIdAndGrupoIdAndNegocio(
                notaId,
                grupoId,
                tenantService.getNegocioActual()
        )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "nota no encontrada"));
        notaPredefinidaRepository.delete(nota);
    }

    @Transactional(readOnly = true)
    public List<NotaPredefinida> resolverNotasSeleccionadasParaProducto(Producto producto, List<Long> notaPredefinidaIds) {
        if (producto == null || producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "producto o categoria invalida");
        }

        Long categoriaId = producto.getCategoria().getId();
        Negocio negocio = tenantService.getNegocioActual();
        List<GrupoNotaCategoria> gruposCategoria = grupoNotaCategoriaRepository
                .findByCategoriaIdAndNegocioOrderByNombreAsc(categoriaId, negocio);

        List<Long> idsRecibidos = notaPredefinidaIds == null ? List.of() : notaPredefinidaIds;
        Set<Long> idsUnicos = new LinkedHashSet<>();
        for (Long notaId : idsRecibidos) {
            if (notaId == null || notaId <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notaPredefinidaId invalido");
            }
            if (!idsUnicos.add(notaId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no se permiten notas duplicadas en el mismo detalle");
            }
        }

        List<NotaPredefinida> notasSeleccionadas = idsUnicos.isEmpty()
                ? new ArrayList<>()
                : notaPredefinidaRepository.findByIdsWithGrupoYCategoria(idsUnicos, negocio);
        if (notasSeleccionadas.size() != idsUnicos.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "una o varias notas predefinidas no existen");
        }

        Map<Long, List<NotaPredefinida>> notasPorGrupo = new HashMap<>();
        for (NotaPredefinida nota : notasSeleccionadas) {
            if (nota.getGrupo() == null || nota.getGrupo().getCategoria() == null || nota.getGrupo().getCategoria().getId() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "nota predefinida sin categoria asociada");
            }
            Long categoriaNotaId = nota.getGrupo().getCategoria().getId();
            if (!categoriaId.equals(categoriaNotaId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "no se permiten notas de grupos ajenos a la categoria del producto"
                );
            }
            notasPorGrupo.computeIfAbsent(nota.getGrupo().getId(), key -> new ArrayList<>()).add(nota);
        }

        for (GrupoNotaCategoria grupo : gruposCategoria) {
            List<NotaPredefinida> seleccionGrupo = notasPorGrupo.getOrDefault(grupo.getId(), List.of());
            if (Boolean.TRUE.equals(grupo.getObligatorio()) && seleccionGrupo.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "falta seleccionar una nota obligatoria del grupo: " + grupo.getNombre()
                );
            }
            if (grupo.getTipoSeleccion() == TipoSeleccionNota.UNICO && seleccionGrupo.size() > 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "el grupo " + grupo.getNombre() + " solo permite una nota seleccionada"
                );
            }
        }

        return notasSeleccionadas;
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        validarId(categoriaId, "categoriaId invalido");
        return categoriaRepository.findByIdAndNegocio(categoriaId, tenantService.getNegocioActual())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria no encontrada"));
    }

    private GrupoNotaCategoria obtenerGrupo(Long categoriaId, Long grupoId) {
        validarId(categoriaId, "categoriaId invalido");
        validarId(grupoId, "grupoId invalido");
        return grupoNotaCategoriaRepository.findByIdAndCategoriaIdAndNegocio(
                grupoId,
                categoriaId,
                tenantService.getNegocioActual()
        )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "grupo de notas no encontrado"));
    }

    private String normalizarNombreGrupo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre del grupo invalido");
        }
        String normalizado = nombre.trim();
        if (normalizado.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "el nombre del grupo supera el maximo permitido");
        }
        return normalizado;
    }

    private String normalizarDescripcionNota(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "descripcion de nota invalida");
        }
        String normalizada = descripcion.trim();
        if (normalizada.length() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "la descripcion de la nota supera el maximo permitido");
        }
        return normalizada;
    }

    private void validarId(Long id, String mensaje) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }
}
