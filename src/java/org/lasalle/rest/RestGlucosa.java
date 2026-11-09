/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lasalle.controller.ControllerMedida;
import org.lasalle.controller.ControllerUsuario;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;

/**
 * Endpoints REST de GlucoAPI.
 *
 * @author mykec
 */
@Path("glucosa")
public class RestGlucosa {

    // ==========================================
    //           ENDPOINTS DE PRUEBA
    // ==========================================

    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar() {
        return Response.ok("""
                { "response" : "Hola desde Java" }
                """).build();
    }

    @Path("saludarPersonalizado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarPersonalizado(
            @QueryParam("nombre") String nombre) {
        String respuesta = """
                           { "response" : "Hola %s desde Java" }
                           """;
        respuesta = String.format(respuesta, nombre != null ? nombre : "invitado");
        return Response.ok(respuesta).build();
    }

    @Path("cuadrado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculo(
            @QueryParam("numero") int numero) {
        int cuadrado = numero * numero;
        String respuesta = """
                           { "response" : "Hola el cuadrado de %s es %s" }
                           """;
        respuesta = String.format(respuesta, numero, cuadrado);
        return Response.ok(respuesta).build();
    }

    // ==========================================
    //           ENDPOINTS DE USUARIOS
    // ==========================================

    /**
     * GET /api/glucosa/getAllUsuarios
     */
    @Path("getAllUsuarios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuario() {
        Gson gson = gson();
        try {
            ControllerUsuario cu = new ControllerUsuario();
            List<Usuario> lista = cu.getUsuarios();
            return Response.ok(gson.toJson(lista)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error al obtener los usuarios", e);
        }
    }

    /**
     * POST /api/glucosa/insertUsuario
     */
    @Path("insertUsuario")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("email") String email) {

        Gson gson = gson();

        if (nombre == null || nombre.isBlank()
                || primerApellido == null || primerApellido.isBlank()
                || email == null || email.isBlank()) {
            return error(Response.Status.BAD_REQUEST,
                    "nombre, primerApellido y email son obligatorios", null);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setPrimerApellido(primerApellido.trim());
        usuario.setSegundoApellido(segundoApellido == null ? null : segundoApellido.trim());
        usuario.setEmail(email.trim());

        try {
            ControllerUsuario cu = new ControllerUsuario();
            usuario.setIdUsuario(cu.insertUsuario(usuario));
            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(usuario))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el usuario", e);
        }
    }

    // ==========================================
    //           ENDPOINTS DE MEDIDAS
    // ==========================================

    /**
     * GET /api/glucosa/getAllMedidas
     *
     * Devuelve todas las medidas registradas.
     */
    @Path("getAllMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedidas() {
        Gson gson = gson();
        try {
            ControllerMedida cm = new ControllerMedida();
            List<Medida> lista = cm.getAllMedidas();
            return Response.ok(gson.toJson(lista)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error al obtener las medidas", e);
        }
    }

    /**
     * GET /api/glucosa/getMedidasByUsuarioId?idUsuario=1
     *
     * Devuelve solo las medidas de un usuario.
     */
    @Path("getMedidasByUsuarioId")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidasByUsuarioId(
            @QueryParam("idUsuario") String idUsuario) {

        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            return error(Response.Status.BAD_REQUEST,
                    "El parametro idUsuario es obligatorio", null);
        }

        int id;
        try {
            id = Integer.parseInt(idUsuario.trim());
        } catch (NumberFormatException e) {
            return error(Response.Status.BAD_REQUEST,
                    "El parametro idUsuario debe ser un numero entero", null);
        }

        if (id <= 0) {
            return error(Response.Status.BAD_REQUEST,
                    "El parametro idUsuario debe ser mayor a 0", null);
        }

        Gson gson = gson();
        try {
            ControllerMedida cm = new ControllerMedida();
            List<Medida> lista = cm.getMedidasByUsuarioId(id);
            return Response.ok(gson.toJson(lista)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error al obtener las medidas del usuario " + id, e);
        }
    }

    /**
     * POST /api/glucosa/insertarMedida (formulario)
     */
    @Path("insertarMedida")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertarMedidaForm(
            @FormParam("idUsuario") Integer idUsuario,
            @FormParam("glucosa") Integer glucosa,
            @FormParam("fechaHora") String fechaHora,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas) {

        if (idUsuario == null || idUsuario <= 0 || glucosa == null || glucosa <= 0) {
            return error(Response.Status.BAD_REQUEST,
                    "idUsuario y glucosa son obligatorios y deben ser mayores a 0", null);
        }

        Medida medida = new Medida(0, idUsuario, glucosa, fechaHora, contexto, notas);
        return guardarMedida(medida);
    }

    /**
     * POST /api/glucosa/insertarMedida (JSON)
     */
    @Path("insertarMedida")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertarMedidaJson(String jsonBody) {
        Medida medida;
        try {
            medida = gson().fromJson(jsonBody, Medida.class);
        } catch (Exception e) {
            return error(Response.Status.BAD_REQUEST, "El JSON enviado no es valido", e);
        }

        if (medida == null || medida.getIdUsuario() <= 0 || medida.getGlucosa() <= 0) {
            return error(Response.Status.BAD_REQUEST,
                    "idUsuario y glucosa son obligatorios y deben ser mayores a 0", null);
        }

        return guardarMedida(medida);
    }

    private Response guardarMedida(Medida medida) {
        Gson gson = gson();
        try {
            ControllerMedida cm = new ControllerMedida();
            cm.insertMedida(medida);
            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(medida))
                    .build();
        } catch (SQLIntegrityConstraintViolationException e) {
            return error(Response.Status.BAD_REQUEST,
                    "El usuario con idUsuario " + medida.getIdUsuario() + " no existe", e);
        } catch (Exception e) {
            e.printStackTrace();
            String mensaje = e.getMessage() == null ? "" : e.getMessage();
            if (mensaje.contains("foreign key constraint fails")) {
                return error(Response.Status.BAD_REQUEST,
                        "El usuario con idUsuario " + medida.getIdUsuario() + " no existe", e);
            }
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la medida", e);
        }
    }

    /**
     * Gson configurado para incluir tambien los campos con valor null.
     */
    private static Gson gson() {
        return new GsonBuilder().serializeNulls().create();
    }

    /**
     * Construye una respuesta de error en JSON.
     */
    private Response error(Response.Status status, String mensaje, Exception e) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("error", mensaje);
        if (e != null && e.getMessage() != null) {
            cuerpo.put("detalle", e.getMessage().replace("\n", " ").replace("\r", ""));
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(gson().toJson(cuerpo))
                .build();
    }
}

