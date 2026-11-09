import java.util.Scanner;

public class Main {

    public static String modificarCadena(String cadenaOrig, int pos, String nuevoC) {
        String izq, der;
        if (pos > 1) {
            izq = cadenaOrig.substring(0, pos - 1);
        } else {
            izq = "";
        }
        if (pos < cadenaOrig.length()) {
            der = cadenaOrig.substring(pos);
        } else {
            der = "";
        }
        return izq + nuevoC + der;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] mapa = new String[41];
        int filas;
        int px = 0, py = 0;
        boolean juegoTerminado = false;
        boolean reiniciarNivel;
        boolean juegoPerdido;
        String tecla;
        String charP, charT, charB, charPNew, charTNew, cT;
        int dx, dy, tx, ty;
        boolean turnoValido, movio, spotValido, balaActiva;

        // Variables de Enemigos y Disparos (Inicializadas en 0)
        int maxEnemigos = 40;
        int k = 0, slotLibre = 0, rx = 0, ry = 0;
        int bxDir = 0, byDir = 0, bX = 0, bY = 0;
        int dxE = 0, dyE = 0, nex = 0, ney = 0;

        int[] eneX = new int[41];
        int[] eneY = new int[41];
        int[] eneHp = new int[41];
        boolean[] eneActivo = new boolean[41];

        // Variables de Puntaje y Probabilidad
        int puntaje, turnosJugados, probActual;

        while (!juegoTerminado) {
            // --- 1. CARGAR MAPA (ARENA) ---
            mapa[1] = "########################################";
            mapa[2] = "#                                      #";
            mapa[3] = "#                                      #";
            mapa[4] = "#                                      #";
            mapa[5] = "#                                      #";
            mapa[6] = "#                                      #";
            mapa[7] = "#                 @                    #";
            mapa[8] = "#                                      #";
            mapa[9] = "#                                      #";
            mapa[10] = "#                                      #";
            mapa[11] = "#                                      #";
            mapa[12] = "#                                      #";
            mapa[13] = "#                                      #";
            mapa[14] = "#                                      #";
            mapa[15] = "########################################";
            filas = 15;

            // --- 2. INICIALIZAR VARIABLES DE SUPERVIVENCIA ---
            for (k = 1; k <= maxEnemigos; k++) {
                eneActivo[k] = false;
            }

            juegoPerdido = false;
            puntaje = 0;
            turnosJugados = 0;
            probActual = 15;

            // --- 3. ENCONTRAR AL JUGADOR ---
            for (int i = 1; i <= filas; i++) {
                for (int j = 1; j <= mapa[i].length(); j++) {
                    charP = mapa[i].substring(j - 1, j);
                    if (charP.equals("@") || charP.equals("+")) {
                        py = i;
                        px = j;
                    }
                }
            }

            reiniciarNivel = false;

            // --- 4. BUCLE INFINITO DE SUPERVIVENCIA ---
            while (!reiniciarNivel && !juegoTerminado) {
                for (int c = 0; c < 3; c++) System.out.println();

                System.out.println("=== ARENA DE SUPERVIVENCIA ===");
                System.out.println("PUNTAJE: " + puntaje + " | Turnos sobrevividos: " + turnosJugados);
                System.out.println("Probabilidad de aparicion enemiga: " + probActual + "%");
                System.out.println("-----------------------------------------------------------------");
                System.out.println("Mover: [W, A, S, D] | Disparo Recto: [I, J, K, L]");
                System.out.println("Disparo Diag.: [U] Arr-Izq | [O] Arr-Der | [N] Aba-Izq | [M] Aba-Der");
                System.out.println("[R] Reiniciar Partida | [Q] Salir del juego");
                System.out.println("Simbolos: @ Tu | $ Caja | o Disparo | 1-4 Enemigos (Vida)");
                System.out.println("-----------------------------------------------------------------");

                for (int i = 1; i <= filas; i++) {
                    System.out.println(mapa[i]);
                }

                if (juegoPerdido) {
                    System.out.println("-----------------------------------------------------------------");
                    System.out.println("¡TE HAN ATRAPADO! Fin del Juego.");
                    System.out.println("Tu puntaje final es: " + puntaje);
                    System.out.println("Sobreviviste " + turnosJugados + " turnos.");
                    System.out.println("Presiona [R] para jugar de nuevo o [Q] para salir.");
                    tecla = scanner.nextLine().trim().toLowerCase();
                    if (tecla.equals("q")) {
                        juegoTerminado = true;
                    } else {
                        reiniciarNivel = true;
                    }
                } else {
                    System.out.println("");
                    System.out.print("Tu accion + ENTER: ");
                    tecla = scanner.nextLine().trim().toLowerCase();
                    turnoValido = false;

                    if (tecla.equals("q")) {
                        juegoTerminado = true;
                    } else if (tecla.equals("r")) {
                        reiniciarNivel = true;
                    } else {
                        dx = 0;
                        dy = 0;
                        bxDir = 0;
                        byDir = 0;

                        switch (tecla) {
                            case "w": dy = -1; turnoValido = true; break;
                            case "s": dy = 1; turnoValido = true; break;
                            case "a": dx = -1; turnoValido = true; break;
                            case "d": dx = 1; turnoValido = true; break;
                            case "i": byDir = -1; turnoValido = true; break;
                            case "k": byDir = 1; turnoValido = true; break;
                            case "j": bxDir = -1; turnoValido = true; break;
                            case "l": bxDir = 1; turnoValido = true; break;
                            case "u": byDir = -1; bxDir = -1; turnoValido = true; break;
                            case "o": byDir = -1; bxDir = 1; turnoValido = true; break;
                            case "n": byDir = 1; bxDir = -1; turnoValido = true; break;
                            case "m": byDir = 1; bxDir = 1; turnoValido = true; break;
                        }

                        // MOVIMIENTO DEL JUGADOR
                        if (dx != 0 || dy != 0) {
                            tx = px + dx;
                            ty = py + dy;

                            if (ty >= 1 && ty <= filas && tx >= 1 && tx <= mapa[1].length()) {
                                charP = mapa[py].substring(px - 1, px);
                                charT = mapa[ty].substring(tx - 1, tx);

                                charPNew = charP.equals("+") ? "." : " ";

                                if (charT.equals(" ") || charT.equals(".")) {
                                    charTNew = charT.equals(".") ? "+" : "@";
                                    mapa[py] = modificarCadena(mapa[py], px, charPNew);
                                    mapa[ty] = modificarCadena(mapa[ty], tx, charTNew);
                                    py = ty;
                                    px = tx;
                                } else if (charT.compareTo("1") >= 0 && charT.compareTo("4") <= 0) {
                                    mapa[py] = modificarCadena(mapa[py], px, " ");
                                    juegoPerdido = true;
                                }
                            }
                        }

                        // DISPARO DEL JUGADOR
                        if (bxDir != 0 || byDir != 0) {
                            bX = px + bxDir;
                            bY = py + byDir;
                            balaActiva = true;

                            while (balaActiva && bY >= 1 && bY <= filas && bX >= 1 && bX <= mapa[1].length()) {
                                charB = mapa[bY].substring(bX - 1, bX);

                                if (charB.equals("#") || charB.equals("$") || charB.equals("*")) {
                                    balaActiva = false;
                                } else if (charB.compareTo("1") >= 0 && charB.compareTo("4") <= 0) {
                                    for (k = 1; k <= maxEnemigos; k++) {
                                        if (eneActivo[k] && eneX[k] == bX && eneY[k] == bY) {
                                            eneHp[k]--;
                                            puntaje += 10;

                                            if (eneHp[k] <= 0) {
                                                eneActivo[k] = false;
                                                puntaje += 50;
                                                mapa[bY] = modificarCadena(mapa[bY], bX, " ");
                                            } else {
                                                mapa[bY] = modificarCadena(mapa[bY], bX, String.valueOf(eneHp[k]));
                                            }
                                        }
                                    }
                                    balaActiva = false;
                                } else {
                                    mapa[bY] = modificarCadena(mapa[bY], bX, "o");

                                    for (int c = 0; c < 3; c++) System.out.println();
                                    System.out.println("=== ARENA DE SUPERVIVENCIA ===");
                                    System.out.println("PUNTAJE: " + puntaje + " | Turnos sobrevividos: " + turnosJugados);
                                    System.out.println("Probabilidad de aparicion enemiga: " + probActual + "%");
                                    System.out.println("-----------------------------------------------------------------");
                                    for (int i = 1; i <= filas; i++) {
                                        System.out.println(mapa[i]);
                                    }

                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }

                                    mapa[bY] = modificarCadena(mapa[bY], bX, charB);
                                    bX += bxDir;
                                    bY += byDir;
                                }
                            }
                        }

                        // FASE ENEMIGOS
                        if (turnoValido && !juegoPerdido) {
                            turnosJugados++;

                            if (turnosJugados % 10 == 0) {
                                probActual += 5;
                                if (probActual > 100) probActual = 100;
                            }

                            // SPAWNER
                            if ((int) (Math.random() * 100) + 1 <= probActual) {
                                slotLibre = 0;
                                k = 1;
                                while (k <= maxEnemigos && slotLibre == 0) {
                                    if (!eneActivo[k]) slotLibre = k;
                                    k++;
                                }

                                if (slotLibre > 0) {
                                    spotValido = false;
                                    while (!spotValido) {
                                        ry = (int) (Math.random() * filas) + 1;
                                        rx = (int) (Math.random() * mapa[1].length()) + 1;
                                        if (ry >= 1 && ry <= filas && rx >= 1 && rx <= mapa[1].length()) {
                                            if (mapa[ry].substring(rx - 1, rx).equals(" ")) {
                                                if (Math.abs(rx - px) + Math.abs(ry - py) > 3) {
                                                    spotValido = true;
                                                }
                                            }
                                        }
                                    }

                                    eneX[slotLibre] = rx;
                                    eneY[slotLibre] = ry;
                                    eneHp[slotLibre] = (int) (Math.random() * 4) + 1;
                                    eneActivo[slotLibre] = true;
                                    mapa[ry] = modificarCadena(mapa[ry], rx, String.valueOf(eneHp[slotLibre]));
                                }
                            }

                            // MOVIMIENTO ENEMIGOS
                            for (k = 1; k <= maxEnemigos; k++) {
                                if (eneActivo[k]) {
                                    dxE = 0;
                                    dyE = 0;

                                    if (px < eneX[k]) dxE = -1;
                                    else if (px > eneX[k]) dxE = 1;

                                    if (py < eneY[k]) dyE = -1;
                                    else if (py > eneY[k]) dyE = 1;

                                    movio = false;

                                    if (dxE != 0 || dyE != 0) {
                                        if (eneY[k] + dyE >= 1 && eneY[k] + dyE <= filas && eneX[k] + dxE >= 1 && eneX[k] + dxE <= mapa[1].length()) {
                                            cT = mapa[eneY[k] + dyE].substring(eneX[k] + dxE - 1, eneX[k] + dxE);
                                            if (cT.equals(" ") || cT.equals("@") || cT.equals("+")) {
                                                nex = eneX[k] + dxE;
                                                ney = eneY[k] + dyE;
                                                movio = true;
                                            } else {
                                                if (dxE != 0) {
                                                    if (eneX[k] + dxE >= 1 && eneX[k] + dxE <= mapa[1].length()) {
                                                        cT = mapa[eneY[k]].substring(eneX[k] + dxE - 1, eneX[k] + dxE);
                                                        if (cT.equals(" ") || cT.equals("@") || cT.equals("+")) {
                                                            nex = eneX[k] + dxE;
                                                            ney = eneY[k];
                                                            movio = true;
                                                        }
                                                    }
                                                }

                                                if (!movio && dyE != 0) {
                                                    if (eneY[k] + dyE >= 1 && eneY[k] + dyE <= filas) {
                                                        cT = mapa[eneY[k] + dyE].substring(eneX[k] - 1, eneX[k]);
                                                        if (cT.equals(" ") || cT.equals("@") || cT.equals("+")) {
                                                            nex = eneX[k];
                                                            ney = eneY[k] + dyE;
                                                            movio = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (movio) {
                                        mapa[eneY[k]] = modificarCadena(mapa[eneY[k]], eneX[k], " ");
                                        eneX[k] = nex;
                                        eneY[k] = ney;

                                        cT = mapa[ney].substring(nex - 1, nex);
                                        if (cT.equals("@") || cT.equals("+")) {
                                            juegoPerdido = true;
                                            mapa[ney] = modificarCadena(mapa[ney], nex, "X");
                                        } else {
                                            mapa[ney] = modificarCadena(mapa[ney], nex, String.valueOf(eneHp[k]));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int c = 0; c < 3; c++) System.out.println();
        System.out.println("Has salido de la Arena. ¡Hasta pronto!");
        scanner.close();
    }
}