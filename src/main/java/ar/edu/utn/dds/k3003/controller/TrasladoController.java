package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoTrasladoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.TrasladoDTO;
import ar.edu.utn.dds.k3003.facades.exceptions.TrasladoNoAsignableException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TrasladoController {

    private final Fachada fachada;

    public TrasladoController(Fachada fachada) {
        this.fachada = fachada;
    }

    public void asignar(Context context) {
        try {
            var trasladoDTO = context.bodyAsClass(TrasladoDTO.class);
            var trasladoDTORta = this.fachada.asignarTraslado(trasladoDTO);
            context.json(trasladoDTORta);
            context.status(HttpStatus.CREATED);
        } catch (TrasladoNoAsignableException | NoSuchElementException e) {
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
        }
    }

    public void obtener(Context context) {
        var id = context.pathParamAsClass("id", Long.class).get();
        try {
            var trasladoDTO = this.fachada.buscarXId(id);
            context.json(trasladoDTO);
        } catch (NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
        }
    }

    public void obtenerPorColaboradorId(Context context) {
        var id = context.queryParamAsClass("id", Long.class).get();
        var anio = context.queryParamAsClass("anio", Integer.class).get();
        var mes = context.queryParamAsClass("mes", Integer.class).get();

        var traslados = this.fachada.trasladosDeColaborador(id, mes, anio);

        // Si no hay traslados, devuelvo un 404
        if (Objects.isNull(traslados) || traslados.isEmpty()) {
            context.result("No se encontraron traslados para el colaborador con id " + id + " en el mes " + mes + " y año " + anio + ".");
            context.status(HttpStatus.NOT_FOUND);
            return;
        }
        context.json(traslados);

    }

    public void modificar(Context context) {
        var id = context.pathParamAsClass("id", Long.class).get();
        try {
            TrasladoDTO trasladoDTO = context.bodyAsClass(TrasladoDTO.class);
            trasladoDTO.setId(id);
            String estado = trasladoDTO.getStatus().toString();

            // Reviso que sea un estado valido, sino devuelvo una excepcion
            switch (estado) {
                case "EN_VIAJE":
                    this.fachada.trasladoRetirado(id);
                    break;
                case "ENTREGADO":
                    this.fachada.trasladoDepositado(id);
                    break;
                default:
                    context.result("Solo se puede cambiar el estado de un traslado a EN_VIAJE o ENTREGADO.");
                    context.status(HttpStatus.BAD_REQUEST);
            }
        }
        catch(NoSuchElementException ex) {
            context.result(ex.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
        }
    }

    public void eliminar(Context ctx) {
        fachada.trasladoRepository.borrarTodo();
        ctx.status(HttpStatus.NO_CONTENT);
    }
}