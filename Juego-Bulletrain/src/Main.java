import java.util.Random;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    public static String modificarCadena(String cadenaOrig, int pos, char nuevoC) {
        int idx = pos - 1;
        if (idx < 0 || idx >= cadenaOrig.length()) {
            return cadenaOrig;
        }
        return cadenaOrig.substring(0, idx) + nuevoC + cadenaOrig.substring(idx + 1);
    }

    public static void borrarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void esperar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void mostrarMenuPrincipal() {
        borrarPantalla();
        System.out.println("==================================================");
        System.out.println("       LABERINTOS DE MONSTRUOS: ARSENAL TOTAL     ");
        System.out.println("==================================================");
        System.out.println("             [ Presiona ENTER para INICIAR ]      ");
        System.out.println("             [ Presiona P   para SALIR ]          ");
        System.out.println("==================================================");
        System.out.println(" Controles:");
        System.out.println(" - Mover: W, A, S, D (Recto) | Q, E, Z, C (Diag)");
        System.out.println(" - Disparo: I, J, K, L (Recto) / U, O, N, M (Diag)");
        System.out.println(" - Acciones: [Espacio] Cambiar Arma | [T] Tienda");
        System.out.println(" - Especiales: [X] Granada | [B] Bombardeo Aéreo");
        System.out.println("==================================================");
    }

    public static void main(String[] args) {
        String[] mapa = new String[41];
        int filas = 15;
        int px = 1, py = 1;

        boolean juegoTerminado = false;
        boolean reiniciarNivel;
        boolean juegoPerdido;
        boolean enMenu;

        String tecla;
        char charP, charT, charB, charPNew, charTNew, cT;
        int dx, dy, tx, ty, bxDir, byDir, bX, bY;
        boolean turnoValido, movio, spotValido, balaActiva;

        // Enemigos
        int maxEnemigos = 40;
        int[] eneX = new int[41];
        int[] eneY = new int[41];
        int[] eneHp = new int[41];
        boolean[] eneActivo = new boolean[41];

        // Jefe
        boolean faseBoss = false;
        boolean bossActivo = false;
        int bossX = 0, bossY = 0, bossHp = 0;

        // Stats
        int puntaje = 0;
        int turnosJugados = 0;
        int probActual = 15;
        int multVida = 1;
        int enemigosRestantes = 10;
        int lastFx = 1, lastFy = 0;
        int danoDisparo;
        int vidaBase;
        int danoRecibido;

        // Inventario
        int armaActual = 1;
        int armadura = 1;
        int muniEscopeta = 0, muniRafaga = 0, muniCohetes = 0;
        int granadas = 0, bombardeos = 0;
        int nvlPistola = 1, nvlEscopeta = 1, nvlRafaga = 1, nvlCohetes = 1;
        String nombreArma;

        while (!juegoTerminado) {
            enMenu = true;
            while (enMenu && !juegoTerminado) {
                mostrarMenuPrincipal();
                tecla = scanner.nextLine().trim();
                if (tecla.isEmpty()) {
                    enMenu = false;
                } else if (tecla.equalsIgnoreCase("esc") || tecla.equalsIgnoreCase("p")) {
                    juegoTerminado = true;
                    enMenu = false;
                }
            }

            if (!juegoTerminado) {
                mapa[1]  = "########################################";
                mapa[2]  = "#                                      #";
                mapa[3]  = "#                                      #";
                mapa[4]  = "#                                      #";
                mapa[5]  = "#                                      #";
                mapa[6]  = "#                                      #";
                mapa[7]  = "#                  @                   #";
                mapa[8]  = "#                                      #";
                mapa[9]  = "#                                      #";
                mapa[10] = "#                                      #";
                mapa[11] = "#                                      #";
                mapa[12] = "#                                      #";
                mapa[13] = "#                                      #";
                mapa[14] = "#                                      #";
                mapa[15] = "########################################";
                filas = 15;

                for (int k = 1; k <= maxEnemigos; k++) {
                    eneActivo[k] = false;
                }

                juegoPerdido = false;
                puntaje = 0;
                turnosJugados = 0;
                probActual = 15;
                multVida = 1;
                enemigosRestantes = 10;
                faseBoss = false;
                bossActivo = false;
                armaActual = 1;
                armadura = 1;
                muniEscopeta = 0;
                muniRafaga = 0;
                muniCohetes = 0;
                granadas = 0;
                bombardeos = 0;
                nvlPistola = 1;
                nvlEscopeta = 1;
                nvlRafaga = 1;
                nvlCohetes = 1;
                lastFx = 1;
                lastFy = 0;

                for (int i = 1; i <= filas; i++) {
                    for (int j = 1; j <= mapa[i].length(); j++) {
                        charP = mapa[i].charAt(j - 1);
                        if (charP == '@' || charP == '+') {
                            py = i;
                            px = j;
                        }
                    }
                }

                reiniciarNivel = false;

                while (!reiniciarNivel && !juegoTerminado) {
                    borrarPantalla();

                    if (armaActual == 1) nombreArma = "Pistola Nv." + nvlPistola + " (Infinito)";
                    else if (armaActual == 2) nombreArma = "Escopeta Nv." + nvlEscopeta + " (" + muniEscopeta + ")";
                    else if (armaActual == 3) nombreArma = "Rafaga Nv." + nvlRafaga + " (" + muniRafaga + ")";
                    else nombreArma = "Cohetes Nv." + nvlCohetes + " (" + muniCohetes + ")";

                    if (faseBoss) {
                        System.out.println("=== LABERINTOS DE MONSTRUOS | ¡BATALLA CONTRA EL JEFE! (NIVEL " + multVida + " DE 15) ===");
                        System.out.println("PUNTAJE: " + puntaje + " | Vida del Jefe: " + bossHp);
                    } else {
                        System.out.println("=== LABERINTOS DE MONSTRUOS | NIVEL (VIDA/DAÑO ENEMIGO): x" + multVida + " DE 15 ===");
                        System.out.println("PUNTAJE: " + puntaje + " | Monstruos para el Jefe: " + enemigosRestantes);
                    }
                    System.out.println("-----------------------------------------------------------------");
                    System.out.println("Arma: " + nombreArma + " | Armadura: " + armadura);
                    System.out.println("Granadas[X]: " + granadas + " | Bombardeo[B]: " + bombardeos + " | Cambiar Arma: [Espacio]");
                    System.out.println("-----------------------------------------------------------------");

                    for (int i = 1; i <= filas; i++) {
                        System.out.println(mapa[i]);
                    }

                    if (juegoPerdido) {
                        System.out.println("-----------------------------------------------------------------");
                        System.out.println("¡HAS MUERTO! Fin del Juego.");
                        System.out.println("Tu puntaje final es: " + puntaje);
                        System.out.println("Presiona [R] para jugar de nuevo o [P] para salir al menú.");
                        scanner.nextLine();
                        reiniciarNivel = true;
                    } else {
                        System.out.print("Tu accion + ENTER: ");
                        tecla = scanner.nextLine().toLowerCase();
                        turnoValido = false;
                        dx = 0; dy = 0;
                        bxDir = 0; byDir = 0;

                        if (tecla.equals("wwssadadba")) {
                            System.out.println("==================================================");
                            System.out.println("            ¡CÓDIGO SECRETO ACTIVADO!             ");
                            System.out.println("==================================================");
                            System.out.print("¿Cuántos puntos deseas tener? ");
                            try {
                                puntaje = Integer.parseInt(scanner.nextLine());
                            } catch (Exception e) {
                                puntaje = 9999;
                            }
                        } else if (tecla.equals("p") || tecla.equals("esc") || tecla.equals("r")) {
                            reiniciarNivel = true;
                        } else if (tecla.equals(" ") || tecla.isEmpty()) {
                            armaActual++;
                            if (armaActual > 4) armaActual = 1;
                        } else if (tecla.equals("t")) {
                            boolean enTienda = true;
                            while (enTienda) {
                                borrarPantalla();
                                System.out.println("==================================================");
                                System.out.println("                 TIENDA DE ARSENAL                ");
                                System.out.println("==================================================");
                                System.out.println("Puntos: " + puntaje);
                                System.out.println(" [1] Mejorar Armadura Nvl " + (armadura + 1) + " (" + (1000 * armadura) + " pts)");
                                System.out.println(" [2] Escopeta +10 Balas     (600 pts)   Tienes: " + muniEscopeta);
                                System.out.println(" [3] Fusil Rafaga +30 Balas (800 pts)   Tienes: " + muniRafaga);
                                System.out.println(" [4] Cohetes +1 Misil       (3000 pts)  Tienes: " + muniCohetes);
                                System.out.println(" [5] Granadas +3            (500 pts)   Tienes: " + granadas);
                                System.out.println(" [6] Bombardeo Aereo +1     (20000 pts) Tienes: " + bombardeos);
                                System.out.println(" --- MEJORAS DE DAÑO ---");
                                System.out.println(" [7] Mejorar Pistola Nvl " + (nvlPistola + 1) + "  (" + (500 * nvlPistola) + " pts)");
                                System.out.println(" [8] Mejorar Escopeta Nvl " + (nvlEscopeta + 1) + " (" + (800 * nvlEscopeta) + " pts)");
                                System.out.println(" [9] Mejorar Fusil Ráfaga Nvl " + (nvlRafaga + 1) + " (" + (800 * nvlRafaga) + " pts)");
                                System.out.println(" [0] Mejorar Cohetes Nvl " + (nvlCohetes + 1) + "  (" + (1000 * nvlCohetes) + " pts)");
                                System.out.println(" [P] Salir de la Tienda");
                                System.out.println("==================================================");
                                System.out.print("Selección: ");
                                String opTienda = scanner.nextLine().toLowerCase();

                                if (opTienda.equals("p") || opTienda.equals("esc")) {
                                    enTienda = false;
                                } else {
                                    if (opTienda.equals("1") && puntaje >= (1000 * armadura)) { puntaje -= (1000 * armadura); armadura++; }
                                    else if (opTienda.equals("2") && puntaje >= 600) { puntaje -= 600; muniEscopeta += 10; }
                                    else if (opTienda.equals("3") && puntaje >= 800) { puntaje -= 800; muniRafaga += 30; }
                                    else if (opTienda.equals("4") && puntaje >= 3000) { puntaje -= 3000; muniCohetes += 1; }
                                    else if (opTienda.equals("5") && puntaje >= 500) { puntaje -= 500; granadas += 3; }
                                    else if (opTienda.equals("6") && puntaje >= 20000) { puntaje -= 20000; bombardeos += 1; }
                                    else if (opTienda.equals("7") && puntaje >= (500 * nvlPistola)) { puntaje -= (500 * nvlPistola); nvlPistola++; }
                                    else if (opTienda.equals("8") && puntaje >= (800 * nvlEscopeta)) { puntaje -= (800 * nvlEscopeta); nvlEscopeta++; }
                                    else if (opTienda.equals("9") && puntaje >= (800 * nvlRafaga)) { puntaje -= (800 * nvlRafaga); nvlRafaga++; }
                                    else if (opTienda.equals("0") && puntaje >= (1000 * nvlCohetes)) { puntaje -= (1000 * nvlCohetes); nvlCohetes++; }
                                }
                            }
                        } else {
                            switch (tecla) {
                                case "w": dy = -1; turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "s": dy = 1;  turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "a": dx = -1; turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "d": dx = 1;  turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "q": dy = -1; dx = -1; turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "e": dy = -1; dx = 1;  turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "z": dy = 1;  dx = -1; turnoValido = true; lastFx = dx; lastFy = dy; break;
                                case "c": dy = 1;  dx = 1;  turnoValido = true; lastFx = dx; lastFy = dy; break;

                                case "i": byDir = -1; bxDir = 0;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "k": byDir = 1;  bxDir = 0;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "j": bxDir = -1; byDir = 0;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "l": bxDir = 1;  byDir = 0;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "u": byDir = -1; bxDir = -1; turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "o": byDir = -1; bxDir = 1;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "n": byDir = 1;  bxDir = -1; turnoValido = true; lastFx = bxDir; lastFy = byDir; break;
                                case "m": byDir = 1;  bxDir = 1;  turnoValido = true; lastFx = bxDir; lastFy = byDir; break;

                                case "x":
                                    if (granadas > 0) {
                                        granadas--;
                                        turnoValido = true;
                                        danoDisparo = 5;
                                        bX = px + (lastFx * 2);
                                        bY = py + (lastFy * 2);
                                        for (int rDy = -1; rDy <= 1; rDy++) {
                                            for (int rDx = -1; rDx <= 1; rDx++) {
                                                tx = bX + rDx; ty = bY + rDy;
                                                if (tx >= 1 && tx <= mapa[1].length() && ty >= 1 && ty <= filas) {
                                                    charB = mapa[ty].charAt(tx - 1);
                                                    if (charB != '#') {
                                                        mapa[ty] = modificarCadena(mapa[ty], tx, '*');
                                                    }
                                                }
                                            }
                                        }
                                        borrarPantalla();
                                        System.out.println("¡B O O M!");
                                        for (int i = 1; i <= filas; i++) System.out.println(mapa[i]);
                                        esperar(150);
                                    } else {
                                        System.out.println("¡No tienes granadas!");
                                        esperar(1000);
                                    }
                                    break;

                                case "b":
                                    if (bombardeos > 0) {
                                        bombardeos--;
                                        turnoValido = true;
                                        if (armadura >= 8) {
                                            armadura -= 8;
                                            System.out.println("¡Sobreviviste al bombardeo! Armadura restante: " + armadura);
                                            esperar(1000);
                                        } else {
                                            juegoPerdido = true;
                                        }
                                    } else {
                                        System.out.println("¡No tienes bombardeos!");
                                        esperar(1000);
                                    }
                                    break;
                            }
                        }

                        if (!reiniciarNivel && !juegoPerdido) {
                            // MOVIMIENTO JUGADOR
                            if (dx != 0 || dy != 0) {
                                tx = px + dx;
                                ty = py + dy;
                                if (ty >= 1 && ty <= filas && tx >= 1 && tx <= mapa[1].length()) {
                                    charP = mapa[py].charAt(px - 1);
                                    charT = mapa[ty].charAt(tx - 1);
                                    charPNew = (charP == '@') ? ' ' : '.';

                                    if (charT == ' ' || charT == '.') {
                                        charTNew = (charT == ' ') ? '@' : '+';
                                        mapa[py] = modificarCadena(mapa[py], px, charPNew);
                                        mapa[ty] = modificarCadena(mapa[ty], tx, charTNew);
                                        py = ty;
                                        px = tx;
                                    }
                                }
                            }

                            // DISPAROS
                            if (bxDir != 0 || byDir != 0) {
                                turnoValido = true;
                                bX = px + bxDir;
                                bY = py + byDir;
                                balaActiva = true;

                                while (balaActiva && bY >= 1 && bY <= filas && bX >= 1 && bX <= mapa[1].length()) {
                                    charB = mapa[bY].charAt(bX - 1);
                                    if (charB == '#') {
                                        balaActiva = false;
                                    } else if (charB != ' ' && charB != '.' && charB != 'o') {
                                        // Impacto a enemigo
                                        for (int k = 1; k <= maxEnemigos; k++) {
                                            if (eneActivo[k] && eneX[k] == bX && eneY[k] == bY) {
                                                eneHp[k] -= nvlPistola;
                                                puntaje += 10;
                                                if (eneHp[k] <= 0) {
                                                    eneActivo[k] = false;
                                                    puntaje += 50;
                                                    mapa[bY] = modificarCadena(mapa[bY], bX, ' ');
                                                    if (!faseBoss) enemigosRestantes--;
                                                } else {
                                                    char cVida = (eneHp[k] <= 9) ? String.valueOf(eneHp[k]).charAt(0) : 'M';
                                                    mapa[bY] = modificarCadena(mapa[bY], bX, cVida);
                                                }
                                            }
                                        }
                                        balaActiva = false;
                                    } else {
                                        mapa[bY] = modificarCadena(mapa[bY], bX, 'o');
                                        borrarPantalla();
                                        System.out.println("=== DISPARANDO ===");
                                        for (int i = 1; i <= filas; i++) System.out.println(mapa[i]);
                                        esperar(20);
                                        mapa[bY] = modificarCadena(mapa[bY], bX, charB);
                                        bX += bxDir;
                                        bY += byDir;
                                    }
                                }
                            }

                            // APARECEN JEFES
                            if (enemigosRestantes <= 0 && !faseBoss) {
                                faseBoss = true;
                                bossActivo = true;
                                bossHp = 7 * multVida;
                                bossX = 5; bossY = 5;
                                mapa[bossY] = modificarCadena(mapa[bossY], bossX, 'B');
                            }

                            // SPAWN Y MOVIMIENTO DE ENEMIGOS
                            if (turnoValido && !juegoPerdido) {
                                turnosJugados++;

                                if (!faseBoss) {
                                    if (turnosJugados % 10 == 0) {
                                        probActual += 5;
                                        if (probActual > 100) probActual = 100;
                                    }

                                    if ((random.nextInt(100) + 1) <= probActual) {
                                        int slotLibre = 0;
                                        int k = 1;
                                        while (k <= maxEnemigos && slotLibre == 0) {
                                            if (!eneActivo[k]) slotLibre = k;
                                            k++;
                                        }

                                        if (slotLibre > 0) {
                                            spotValido = false;
                                            int ry = 1, rx = 1;
                                            while (!spotValido) {
                                                ry = random.nextInt(filas) + 1;
                                                rx = random.nextInt(mapa[1].length()) + 1;
                                                if (mapa[ry].charAt(rx - 1) == ' ' && (Math.abs(rx - px) + Math.abs(ry - py)) > 3) {
                                                    spotValido = true;
                                                }
                                            }
                                            eneX[slotLibre] = rx;
                                            eneY[slotLibre] = ry;
                                            vidaBase = random.nextInt(4) + 1;
                                            eneHp[slotLibre] = vidaBase * multVida;
                                            eneActivo[slotLibre] = true;

                                            char cVida = (eneHp[slotLibre] <= 9) ? String.valueOf(eneHp[slotLibre]).charAt(0) : 'M';
                                            mapa[ry] = modificarCadena(mapa[ry], rx, cVida);
                                        }
                                    }
                                }

                                // MOVER ENEMIGOS HACIA EL JUGADOR
                                for (int k = 1; k <= maxEnemigos; k++) {
                                    if (eneActivo[k]) {
                                        int dxE = 0, dyE = 0;
                                        if (px < eneX[k]) dxE = -1; else if (px > eneX[k]) dxE = 1;
                                        if (py < eneY[k]) dyE = -1; else if (py > eneY[k]) dyE = 1;

                                        movio = false;
                                        int nex = eneX[k], ney = eneY[k];

                                        if (dxE != 0) {
                                            if (eneX[k] + dxE >= 1 && eneX[k] + dxE <= mapa[1].length()) {
                                                cT = mapa[eneY[k]].charAt(eneX[k] + dxE - 1);
                                                if (cT == ' ' || cT == '@' || cT == '+') {
                                                    nex = eneX[k] + dxE; ney = eneY[k]; movio = true;
                                                }
                                            }
                                        } else if (dyE != 0) {
                                            if (eneY[k] + dyE >= 1 && eneY[k] + dyE <= filas) {
                                                cT = mapa[eneY[k] + dyE].charAt(eneX[k] - 1);
                                                if (cT == ' ' || cT == '@' || cT == '+') {
                                                    nex = eneX[k]; ney = eneY[k] + dyE; movio = true;
                                                }
                                            }
                                        }

                                        if (movio) {
                                            mapa[eneY[k]] = modificarCadena(mapa[eneY[k]], eneX[k], ' ');
                                            cT = mapa[ney].charAt(nex - 1);
                                            eneX[k] = nex; eneY[k] = ney;

                                            if (cT == '@' || cT == '+') {
                                                danoRecibido = eneHp[k];
                                                if (armadura >= danoRecibido) {
                                                    armadura -= danoRecibido;
                                                    eneActivo[k] = false;
                                                    puntaje += 50;
                                                    if (!faseBoss) enemigosRestantes--;
                                                } else {
                                                    juegoPerdido = true;
                                                }
                                            } else {
                                                char cVida = (eneHp[k] <= 9) ? String.valueOf(eneHp[k]).charAt(0) : 'M';
                                                mapa[ney] = modificarCadena(mapa[ney], nex, cVida);
                                            }
                                        }
                                    }
                                }
                            }

                            // COMPROBAR VICTORIA
                            if (faseBoss && !bossActivo) {
                                if (multVida >= 15) {
                                    borrarPantalla();
                                    System.out.println("¡VICTORIA SUPREMA! Has superado el nivel 15.");
                                    scanner.nextLine();
                                    reiniciarNivel = true;
                                } else {
                                    multVida++;
                                    enemigosRestantes = 10;
                                    faseBoss = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        borrarPantalla();
        System.out.println("Has salido del laberinto. ¡Hasta pronto!");
    }
}