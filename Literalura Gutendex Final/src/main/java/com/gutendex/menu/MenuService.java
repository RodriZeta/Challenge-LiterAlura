package com.gutendex.menu;

import com.gutendex.service.LibroService;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class MenuService {

    private final LibroService libroService;
    private final Scanner scanner = new Scanner(System.in);

    public MenuService(LibroService libroService) {
        this.libroService = libroService;
    }

    public void mostrarMenu() {
        while (true) {
            System.out.println();
            System.out.println("========== 📚 BIENVENIDOS A LA APP LITERALURA GUTENDEX ==========");
            System.out.println("1) 🔎 Buscar libros por título");
            System.out.println("2) 👤 Buscar autores por nombre");
            System.out.println("3) 📖 Listar libros registrados");
            System.out.println("4) 🖋️ Listar autores registrados");
            System.out.println("5) 🌐 Listar libros por idioma");
            System.out.println("6) 🏆 Top 10 libros por descargas");
            System.out.println("7) ⏳ Autores vivos en un año");
            System.out.println("8) 🚪 Salir");
            System.out.print("Ingrese una opción: ");

            String opt = scanner.nextLine().trim();
            if ("8".equals(opt)) {
                System.out.println("👋 ¡Muchas gracias por usar nuestra App... Hasta Pronto!");
                return;
            }

            switch (opt) {
                case "1":
                    System.out.print("🔍 Ingrese un título: ");
                    String titulo = scanner.nextLine().trim();
                    if (!titulo.isEmpty()) libroService.buscarLibroPorTitulo(titulo);
                    break;
                case "2":
                    System.out.print("🔍 Ingrese un autor: ");
                    String autor = scanner.nextLine().trim();
                    if (!autor.isEmpty()) libroService.buscarAutoresPorNombre(autor);
                    break;
                case "3":
                    libroService.listarLibros();
                    break;
                case "4":
                    libroService.listarAutores();
                    break;
                case "5":
                    System.out.print("Ingrese el código de idioma (es, en, fr, ...): ");
                    String idioma = scanner.nextLine().trim();
                    if (!idioma.isEmpty()) libroService.listarLibrosPorIdioma(idioma);
                    break;
                case "6":
                    libroService.top10Descargas();
                    break;
                case "7":
                    System.out.print("Ingrese el año (e.g., 1900): ");
                    String sAnio = scanner.nextLine().trim();
                    try {
                        int anio = Integer.parseInt(sAnio);
                        libroService.autoresVivosEnAnio(anio);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Año inválido.");
                    }
                    break;
                default:
                    System.out.println("❌ Opción inválida.");
            }
        }
    }
}
