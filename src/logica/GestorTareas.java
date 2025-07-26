package logica;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modelo.Tarea;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestorTareas {
    private List<Tarea> listaTareas;
    private final String rutaArchivo = "data/tareas.json";
    private final Gson gson = new Gson();

    public GestorTareas() {
        listaTareas = new ArrayList<>();
        cargarDesdeArchivo();
    }

    public void agregarTarea(Tarea tarea) {
        listaTareas.add(tarea);
        guardarEnArchivo();
    }

    public void eliminarTarea(int indice) {
        if (indice >= 0 && indice < listaTareas.size()) {
            listaTareas.remove(indice);
            guardarEnArchivo();
        }
    }

    public void editarTarea(int indice, Tarea nueva) {
        if (indice >= 0 && indice < listaTareas.size()) {
            listaTareas.set(indice, nueva);
            guardarEnArchivo();
        }
    }

    public void marcarCompletada(int indice, boolean estado) {
        if (indice >= 0 && indice < listaTareas.size()) {
            listaTareas.get(indice).setCompletada(estado);
            guardarEnArchivo();
        }
    }

    public List<Tarea> obtenerTareas() {
        return listaTareas;
    }

    private void guardarEnArchivo() {
        try (Writer writer = new FileWriter(rutaArchivo)) {
            gson.toJson(listaTareas, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarDesdeArchivo() {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) return;

        try (Reader reader = new FileReader(archivo)) {
            Type listType = new TypeToken<ArrayList<Tarea>>() {}.getType();
            listaTareas = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
