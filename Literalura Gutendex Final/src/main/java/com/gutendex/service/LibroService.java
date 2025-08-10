package com.gutendex.service;

import com.gutendex.client.GutendexClient;
import com.gutendex.dto.AuthorDTO;
import com.gutendex.dto.BookDTO;
import com.gutendex.dto.GutendexResponse;
import com.gutendex.entity.Autor;
import com.gutendex.entity.Libro;
import com.gutendex.repository.AutorRepository;
import com.gutendex.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final GutendexClient client;
    private final Scanner scanner = new Scanner(System.in);

    public LibroService(LibroRepository libroRepository, AutorRepository autorRepository, GutendexClient client) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.client = client;
    }

    // ====== BÚSQUEDAS EN API ======
    public void buscarLibroPorTitulo(String titulo) {
        GutendexResponse resp = client.buscar(titulo);
        if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) {
            System.out.println("😕 No se encontraron resultados.");
            return;
        }

        List<BookDTO> books = resp.getResults();
        System.out.println("🔎 Resultados:");
        for (int i = 0; i < books.size(); i++) {
            BookDTO b = books.get(i);
            String idioma = (b.getLanguages() != null && !b.getLanguages().isEmpty()) ? b.getLanguages().get(0) : "n/a";
            String autores = (b.getAuthors() == null || b.getAuthors().isEmpty()) ? "Desconocido"
                    : b.getAuthors().stream().map(AuthorDTO::getName).collect(Collectors.joining(", "));
            System.out.printf("%d) %s — %s (%s) — Autores: %s%n", i + 1, b.getTitle(), idioma.equalsIgnoreCase("es") ? "Español" : "Inglés", idioma, autores);
        }

        System.out.print("¿Guardar? [n = ninguno, número = índice, a = todos]: ");
        String opt = scanner.nextLine().trim();

        if ("n".equalsIgnoreCase(opt)) {
            System.out.println("👌 Nada guardado.");
            return;
        } else if ("a".equalsIgnoreCase(opt)) {
            int guardados = 0;
            for (BookDTO b : books) {
                if (guardarLibroDesdeApi(b)) guardados++;
            }
            System.out.println("✅ Guardados " + guardados + " libros.");
        } else if (opt.matches("\\d+")) {
            int idx = Integer.parseInt(opt);
            if (idx >= 1 && idx <= books.size()) {
                if (guardarLibroDesdeApi(books.get(idx - 1))) {
                    System.out.println("✅ Libro guardado.");
                } else {
                    System.out.println("ℹ️ Ya existía o no se pudo guardar.");
                }
            } else {
                System.out.println("❌ Índice fuera de rango.");
            }
        } else {
            System.out.println("❌ Opción inválida.");
        }
    }

    public void buscarAutoresPorNombre(String nombre) {
        GutendexResponse resp = client.buscar(nombre);
        if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) {
            System.out.println("😕 No se encontraron resultados.");
            return;
        }
        // aplanar autores
        List<AuthorDTO> autores = resp.getResults().stream()
                .filter(b -> b.getAuthors() != null)
                .flatMap(b -> b.getAuthors().stream())
                .collect(Collectors.toList());

        if (autores.isEmpty()) {
            System.out.println("😕 No se encontraron autores.");
            return;
        }

        System.out.println("🔎 Autores encontrados:");
        for (int i = 0; i < autores.size(); i++) {
            AuthorDTO a = autores.get(i);
            System.out.printf("%d) %s (nac: %s, fall: %s)%n", i + 1, a.getName(),
                    a.getBirth_year() == null ? "?" : a.getBirth_year(),
                    a.getDeath_year() == null ? "?" : a.getDeath_year());
        }

        System.out.print("¿Guardar? [n = ninguno, número = índice, a = todos]: ");
        String opt = scanner.nextLine().trim();

        if ("n".equalsIgnoreCase(opt)) {
            System.out.println("👌 Nada guardado.");
            return;
        } else if ("a".equalsIgnoreCase(opt)) {
            int guardados = 0;
            for (AuthorDTO a : autores) {
                if (guardarAutorDesdeApi(a)) guardados++;
            }
            System.out.println("✅ Guardados " + guardados + " autores.");
        } else if (opt.matches("\\d+")) {
            int idx = Integer.parseInt(opt);
            if (idx >= 1 && idx <= autores.size()) {
                if (guardarAutorDesdeApi(autores.get(idx - 1))) {
                    System.out.println("✅ Autor guardado.");
                } else {
                    System.out.println("ℹ️ Ya existía o no se pudo guardar.");
                }
            } else {
                System.out.println("❌ Índice fuera de rango.");
            }
        } else {
            System.out.println("❌ Opción inválida.");
        }
    }

    // ====== PERSISTENCIA ======
    @Transactional
    public boolean guardarLibroDesdeApi(BookDTO b) {
        if (b == null) return false;

        String idioma = (b.getLanguages() != null && !b.getLanguages().isEmpty()) ? b.getLanguages().get(0) : "n/a";
        String titulo = b.getTitle() == null ? "(Sin título)" : b.getTitle().trim();
        // Airbag por si la BD no fue migrada aún (aunque titulo es TEXT)
        if (titulo.length() > 10000) {
            titulo = titulo.substring(0, 10000);
        }
        int descargas = b.getDownload_count();

        // Evitar duplicados por título (case-insensitive)
        if (libroRepository.findFirstByTituloIgnoreCase(titulo).isPresent()) {
            return false;
        }

        Libro libro = new Libro(titulo, idioma, descargas);

        // Vincular autores existentes o crearlos si no existen
        Set<Autor> autores = new HashSet<>();
        if (b.getAuthors() != null) {
            for (AuthorDTO adto : b.getAuthors()) {
                String nombreAutor = adto.getName().trim();
                Autor autor = autorRepository.findFirstByNombreIgnoreCase(nombreAutor)
                        .orElseGet(() -> autorRepository.save(new Autor(
                                nombreAutor,
                                adto.getBirth_year(),
                                adto.getDeath_year()
                        )));
                autores.add(autor);
            }
        }
        libro.setAutores(autores);
        libroRepository.save(libro);
        return true;
    }

    @Transactional
    public boolean guardarAutorDesdeApi(AuthorDTO a) {
        if (a == null || a.getName() == null || a.getName().trim().isEmpty()) return false;
        String nombre = a.getName().trim();
        if (autorRepository.findFirstByNombreIgnoreCase(nombre).isPresent()) {
            return false;
        }
        Autor autor = new Autor(nombre, a.getBirth_year(), a.getDeath_year());
        autorRepository.save(autor);
        return true;
    }

    // ====== CONSULTAS LOCALES ======
    public void listarLibros() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }
        System.out.println("📖 Libros registrados:");
        for (Libro l : libros) {
            String autores = l.getAutores().stream().map(Autor::getNombre).collect(Collectors.joining(", "));
            String idiomaLargo = idiomaBonito(l.getIdioma());
            System.out.printf("- %s — %s (%s) — Autores: %s%n", l.getTitulo(), idiomaLargo, l.getIdioma(), autores.isEmpty()? "Desconocido" : autores);
        }
    }

    public void listarAutores() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
            return;
        }
        System.out.println("🖋️ Autores registrados:");
        for (Autor a : autores) {
            System.out.printf("- %s (nac: %s, fall: %s)%n", a.getNombre(),
                    a.getAnioNacimiento() == null ? "?" : a.getAnioNacimiento(),
                    a.getAnioFallecimiento() == null ? "?" : a.getAnioFallecimiento());
        }
    }

    public void listarLibrosPorIdioma(String idioma) {
        List<Libro> libros = libroRepository.findByIdiomaIgnoreCase(idioma);
        if (libros.isEmpty()) {
            System.out.println("No hay libros para el idioma: " + idioma);
            return;
        }
        System.out.println("📚 Libros en idioma " + idiomaBonito(idioma) + " (" + idioma + "):");
        for (Libro l : libros) {
            String autores = l.getAutores().stream().map(Autor::getNombre).collect(Collectors.joining(", "));
            System.out.printf("- %s — Autores: %s%n", l.getTitulo(), autores.isEmpty()? "Desconocido" : autores);
        }
    }

    public void top10Descargas() {
        List<Libro> top = libroRepository.top10PorDescargas();
        if (top.isEmpty()) {
            System.out.println("No hay datos suficientes.");
            return;
        }
        System.out.println("🏆 Top 10 por descargas:");
        int i = 1;
        for (Libro l : top) {
            System.out.printf("%d) %s — %d descargas%n", i++, l.getTitulo(), l.getDescargas() == null ? 0 : l.getDescargas());
        }
    }

    public void autoresVivosEnAnio(int anio) {
        List<Autor> vivos = autorRepository.autoresVivosEn(anio);
        if (vivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en " + anio);
            return;
        }
        System.out.println("👤 Autores vivos en " + anio + ":");
        for (Autor a : vivos) {
            System.out.printf("- %s (nac: %s, fall: %s)%n", a.getNombre(),
                    a.getAnioNacimiento() == null ? "?" : a.getAnioNacimiento(),
                    a.getAnioFallecimiento() == null ? "?" : a.getAnioFallecimiento());
        }
    }

    private String idiomaBonito(String code) {
        if (code == null) return "n/a";
        switch (code.toLowerCase()) {
            case "es": return "Español";
            case "en": return "Inglés";
            case "fr": return "Francés";
            case "pt": return "Portugués";
            case "de": return "Alemán";
            case "it": return "Italiano";
            default: return "Idioma";
        }
    }
}
