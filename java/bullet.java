import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.*;

public class Bullet {

    // --- CONFIGURACIÓN DE TECLAS Y DIRECCIONES ---
    private static final Map<Character, int[]> MOVIMIENTO = new HashMap<>();
    private static final Map<Character, int[]> DISPARO = new HashMap<>();

    static {
        // Movimiento: WASD (recto) y QEZC (diagonal)
        MOVIMIENTO.put('w', new int[]{0, -1});  MOVIMIENTO.put('s', new int[]{0, 1});
        MOVIMIENTO.put('a', new int[]{-1, 0});  MOVIMIENTO.put('d', new int[]{1, 0});
        MOVIMIENTO.put('q', new int[]{-1, -1}); MOVIMIENTO.put('e', new int[]{1, -1});
        MOVIMIENTO.put('z', new int[]{-1, 1});  MOVIMIENTO.put('c', new int[]{1, 1});

        // Disparo: IJKL (recto) y UONM (diagonal)
        DISPARO.put('i', new int[]{0, -1});  DISPARO.put('k', new int[]{0, 1});
        DISPARO.put('j', new int[]{-1, 0});  DISPARO.put('l', new int[]{1, 0});
        DISPARO.put('u', new int[]{-1, -1}); DISPARO.put('o', new int[]{1, -1});
        DISPARO.put('n', new int[]{-1, 1});  DISPARO.put('m', new int[]{1, 1});
    }

    // --- ESTRUCTURAS DE DATOS DE ENTIDADES ---
    static class Arma {
        String nombre;
        int nvl;
        int muni;

        Arma(String nombre, int nvl, int muni) {
            this.nombre = nombre;
            this.nvl = nvl;
            this.muni = muni;
        }
    }

    static class Enemigo {
        int x, y, hp;

        Enemigo(int x, int y, int hp) {
            this.x = x;
            this.y = y;
            this.hp = hp;
        }
    }

    // --- ESTADO DEL JUEGO ---
    private final Screen screen;
    private final TextGraphics tg;
    private final Random random = new Random();

    private int puntaje;
    private int armadura;
    private int multVida;
    private final Map<Integer, Arma> armas = new HashMap<>();
    private int armaActual;
    private int granadas;
    private int bombardeos;
    private boolean juegoGanado;
    private boolean juegoPerdido;
    private int enemigosMeta;
    private int enemigosRestantes;

    private final char[][] mapa = new char[15][40];
    private int px, py;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private Enemigo jefe = null;
    private boolean faseJefe;
    private int turnos;
    private String msgAlerta = "";

    public Bullet() throws IOException {
        Terminal terminal = new DefaultTerminalFactory()
                .setPreferTerminalEmulator(true)
                .createTerminal();
        this.screen = new TerminalScreen(terminal);
        this.screen.startScreen();
        this.screen.setCursorPosition(null);
        this.tg = screen.newTextGraphics();
    }

    public void reiniciarStatsGlobales() {
        puntaje = 0;
        armadura = 1;
        multVida = 1;
        armas.clear();
        armas.put(1, new Arma("Pistola", 1, -1));
        armas.put(2, new Arma("Escopeta", 1, 0));
        armas.put(3, new Arma("Ráfaga", 1, 0));
        armas.put(4, new Arma("Cohetes", 1, 0));
        armaActual = 1;
        granadas = 0;
        bombardeos = 0;
        juegoGanado = false;
        enemigosMeta = 10;
        iniciarNivel();
    }

    public void iniciarNivel() {
        String[] mapaStr = {
            "########################################",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                  @                   #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "#                                      #",
            "########################################"
        };
        for (int y = 0; y < 15; y++) {
            mapa[y] = mapaStr[y].toCharArray();
        }
        py = 6;
        px = 19;
        enemigos.clear();
        enemigosRestantes = enemigosMeta;
        jefe = null;
        faseJefe = false;
        turnos = 0;
        juegoPerdido = false;
        msgAlerta = "";
    }

    public void dibujar() {
        screen.clear();
        Arma arma = armas.get(armaActual);
        String muniStr = (arma.muni == -1) ? "Inf" : String.valueOf(arma.muni);

        String titulo = !faseJefe ? 
            "=== LABERINTOS DE MONSTRUOS | NIVEL x" + multVida + " ===" : 
            "=== ¡BATALLA CONTRA EL JEFE! (NIVEL x" + multVida + ") ===";

        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.enableModifiers(SGR.BOLD);
        tg.putString(0, 0, titulo);
        tg.disableModifiers(SGR.BOLD);

        tg.putString(0, 1, String.format("PUNTAJE: %05d | Restantes para Jefe: %d", puntaje, Math.max(0, enemigosRestantes)));

        tg.setForegroundColor(TextColor.ANSI.GREEN);
        tg.putString(0, 2, String.format("Arma: %s Nv.%d (%s) | Armadura: %d", arma.nombre, arma.nvl, muniStr, armadura));

        tg.setForegroundColor(TextColor.ANSI.YELLOW);
        tg.putString(0, 3, String.format("Granadas[X]: %d | Bombardeo[B]: %d | Cambiar[Espacio]", granadas, bombardeos));

        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.putString(0, 4, "Mover: recto:WASD/diagonal:QEZC | Disparar: recto:IJKL/diagonal:UONM | Tienda: [T]");

        if (!msgAlerta.isEmpty()) {
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            tg.enableModifiers(SGR.BOLD);
            tg.putString(0, 5, ">> " + msgAlerta + " <<");
            tg.disableModifiers(SGR.BOLD);
        } else {
            tg.setForegroundColor(TextColor.ANSI.WHITE);
            tg.putString(0, 5, "---------------------------------------------------------------------------");
        }

        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 40; x++) {
                char ch = mapa[y][x];
                tg.disableModifiers(SGR.BOLD);

                if (ch == '#') {
                    tg.setForegroundColor(TextColor.ANSI.CYAN);
                } else if (ch == '@') {
                    tg.setForegroundColor(TextColor.ANSI.GREEN);
                    tg.enableModifiers(SGR.BOLD);
                } else if (ch == 'B') {
                    tg.setForegroundColor(TextColor.ANSI.MAGENTA);
                    tg.enableModifiers(SGR.BOLD);
                } else if (Character.isDigit(ch) || ch == 'M') {
                    tg.setForegroundColor(TextColor.ANSI.RED);
                } else if (ch == '*' || ch == 'R') {
                    tg.setForegroundColor(TextColor.ANSI.YELLOW);
                    tg.enableModifiers(SGR.BOLD);
                } else {
                    tg.setForegroundColor(TextColor.ANSI.WHITE);
                }

                tg.putString(x, y + 5, String.valueOf(ch));
            }
        }
        try {
            screen.refresh();
        } catch (IOException ignored) {}
    }

    public void spawnEnemigo() {
        if (faseJefe || enemigosRestantes <= 0) return;

        int probActual = Math.min(100, 15 + (turnos / 10) * 5);
        if (random.nextInt(100) + 1 > probActual) return;
        if (enemigos.size() >= 40) return;

        while (true) {
            int rx = random.nextInt(38) + 1;
            int ry = random.nextInt(13) + 1;
            if (mapa[ry][rx] == ' ' && (Math.abs(rx - px) + Math.abs(ry - py)) > 3) {
                int hp = (random.nextInt(4) + 1) * multVida;
                enemigos.add(new Enemigo(rx, ry, hp));
                mapa[ry][rx] = (hp <= 9) ? (char) ('0' + hp) : 'M';
                break;
            }
        }
    }

    public void spawnJefe() {
        msgAlerta = "¡HA APARECIDO EL JEFE!";
        while (true) {
            int rx = random.nextInt(38) + 1;
            int ry = random.nextInt(13) + 1;
            if (mapa[ry][rx] == ' ' && (Math.abs(rx - px) + Math.abs(ry - py)) > 5) {
                jefe = new Enemigo(rx, ry, 7 * multVida);
                mapa[ry][rx] = 'B';
                break;
            }
        }
    }

    public void explosion(int cx, int cy, int radio, int dano) {
        msgAlerta = "¡BOOM!";
        List<int[]> afectados = new ArrayList<>();

        for (int dy = -radio; dy <= radio; dy++) {
            for (int dx = -radio; dx <= radio; dx++) {
                int nx = cx + dx;
                int ny = cy + dy;
                if (ny >= 1 && ny <= 13 && nx >= 1 && nx <= 38 && mapa[ny][nx] != '#') {
                    afectados.add(new int[]{nx, ny});
                    mapa[ny][nx] = '*';
                }
            }
        }

        dibujar();
        try { Thread.sleep(150); } catch (InterruptedException ignored) {}

        Set<String> procesados = new HashSet<>();
        for (int[] pos : afectados) {
            int nx = pos[0];
            int ny = pos[1];
            String key = nx + "," + ny;
            if (procesados.contains(key)) continue;
            procesados.add(key);

            mapa[ny][nx] = ' ';

            for (int i = enemigos.size() - 1; i >= 0; i--) {
                Enemigo ene = enemigos.get(i);
                if (ene.x == nx && ene.y == ny) {
                    ene.hp -= dano;
                    puntaje += 10;
                    if (ene.hp <= 0) {
                        enemigos.remove(i);
                        puntaje += 50;
                        enemigosRestantes--;
                    } else {
                        mapa[ny][nx] = (ene.hp <= 9) ? (char) ('0' + ene.hp) : 'M';
                    }
                }
            }

            if (jefe != null && jefe.x == nx && jefe.y == ny) {
                jefe.hp -= dano;
                puntaje += 20;
                if (jefe.hp <= 0) {
                    jefe = null;
                    puntaje += 200;
                } else {
                    mapa[ny][nx] = 'B';
                }
            }
        }
        mapa[py][px] = '@';
    }

    public void tienda() throws IOException {
        StringBuilder secuenciaTruco = new StringBuilder();

        while (true) {
            screen.clear();
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            tg.enableModifiers(SGR.BOLD);
            tg.putString(0, 0, "=== TIENDA DE ARSENAL ===");
            tg.disableModifiers(SGR.BOLD);

            tg.setForegroundColor(TextColor.ANSI.WHITE);
            tg.putString(0, 1, "Puntos: " + puntaje);

            Arma p = armas.get(1), e = armas.get(2), r = armas.get(3), c = armas.get(4);

            String[] opciones = {
                String.format("[1] Mejorar Armadura Nvl %d (%d pts)", armadura + 1, 1000 * armadura),
                String.format("[2] Escopeta +10 Balas      (600 pts)  Tienes: %d", e.muni),
                String.format("[3] Ráfaga +30 Balas        (800 pts)  Tienes: %d", r.muni),
                String.format("[4] Cohetes +1 Misil        (3000 pts) Tienes: %d", c.muni),
                String.format("[5] Granadas +3             (500 pts)  Tienes: %d", granadas),
                String.format("[6] Bombardeo +1            (20000 pts)Tienes: %d", bombardeos),
                String.format("[7] Mejorar Pistola Nvl %d  (%d pts)", p.nvl + 1, 500 * p.nvl),
                String.format("[8] Mejorar Escopeta Nvl %d (%d pts)", e.nvl + 1, 800 * e.nvl),
                String.format("[9] Mejorar Ráfaga Nvl %d   (%d pts)", r.nvl + 1, 800 * r.nvl),
                String.format("[0] Mejorar Cohetes Nvl %d  (%d pts)", c.nvl + 1, 1000 * c.nvl),
                "Presiona P o ESC para salir."
            };

            for (int i = 0; i < opciones.length; i++) {
                tg.putString(0, 3 + i, opciones[i]);
            }
            screen.refresh();

            KeyStroke key = screen.readInput();
            char ch = Character.toLowerCase(key.getCharacter() != null ? key.getCharacter() : ' ');

            if (Character.isLetter(ch)) {
                secuenciaTruco.append(ch);
                if (secuenciaTruco.length() > 10) {
                    secuenciaTruco.delete(0, secuenciaTruco.length() - 10);
                }
                if (secuenciaTruco.toString().equals("wwssadadba")) {
                    puntaje += 100000000;
                    secuenciaTruco.setLength(0);
                    continue;
                }
            }

            if (ch == 'p' || key.getKeyType() == KeyType.Escape) break;

            int costo = -1;
            Runnable accion = null;

            switch (ch) {
                case '1':
                    costo = 1000 * armadura;
                    accion = () -> armadura++;
                    break;
                case '2':
                    costo = 600;
                    accion = () -> e.muni += 10;
                    break;
                case '3':
                    costo = 800;
                    accion = () -> r.muni += 30;
                    break;
                case '4':
                    costo = 3000;
                    accion = () -> c.muni += 1;
                    break;
                case '5':
                    costo = 500;
                    accion = () -> granadas += 3;
                    break;
                case '6':
                    costo = 20000;
                    accion = () -> bombardeos += 1;
                    break;
                case '7':
                    costo = 500 * p.nvl;
                    accion = () -> p.nvl++;
                    break;
                case '8':
                    costo = 800 * e.nvl;
                    accion = () -> e.nvl++;
                    break;
                case '9':
                    costo = 800 * r.nvl;
                    accion = () -> r.nvl++;
                    break;
                case '0':
                    costo = 1000 * c.nvl;
                    accion = () -> c.nvl++;
                    break;
            }

            if (costo != -1 && puntaje >= costo && accion != null) {
                puntaje -= costo;
                accion.run();
            }
        }
    }

    public void disparar(int bxDir, int byDir) {
        Arma arma = armas.get(armaActual);
        if (armaActual != 1 && arma.muni <= 0) {
            msgAlerta = "¡Sin munición!";
            return;
        }

        int balas = (armaActual == 1 || armaActual == 4) ? 1 : (armaActual == 3 ? 3 : 1);
        if (armaActual == 3) arma.muni -= 3;
        else if (armaActual == 2 || armaActual == 4) arma.muni -= 1;

        if (armaActual == 2) {
            msgAlerta = "¡Boom de Escopeta!";
            int dano = 2 * arma.nvl;
            List<int[]> afectados = new ArrayList<>();

            for (int d = 1; d <= 3; d++) {
                for (int w = -1; w <= 1; w++) {
                    int nx = px + (d * bxDir) + (byDir != 0 ? w : 0);
                    int ny = py + (d * byDir) + (bxDir != 0 ? w : 0);
                    if (ny >= 1 && ny <= 13 && nx >= 1 && nx <= 38 && mapa[ny][nx] != '#') {
                        afectados.add(new int[]{nx, ny});
                        mapa[ny][nx] = '*';
                    }
                }
            }

            dibujar();
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            Set<String> procesados = new HashSet<>();
            for (int[] pos : afectados) {
                int nx = pos[0];
                int ny = pos[1];
                String key = nx + "," + ny;
                if (procesados.contains(key)) continue;
                procesados.add(key);

                mapa[ny][nx] = ' ';

                for (int i = enemigos.size() - 1; i >= 0; i--) {
                    Enemigo ene = enemigos.get(i);
                    if (ene.x == nx && ene.y == ny) {
                        ene.hp -= dano;
                        puntaje += 10;
                        if (ene.hp <= 0) {
                            enemigos.remove(i);
                            puntaje += 50;
                            enemigosRestantes--;
                        } else {
                            mapa[ny][nx] = (ene.hp <= 9) ? (char) ('0' + ene.hp) : 'M';
                        }
                    }
                }

                if (jefe != null && jefe.x == nx && jefe.y == ny) {
                    jefe.hp -= dano;
                    puntaje += 20;
                    if (jefe.hp <= 0) {
                        jefe = null;
                        puntaje += 200;
                    } else {
                        mapa[ny][nx] = 'B';
                    }
                }
            }
            mapa[py][px] = '@';
            return;

        } else if (armaActual == 4) {
            int bx = px + bxDir;
            int by = py + byDir;
            while (mapa[by][bx] == ' ') {
                mapa[by][bx] = 'R';
                dibujar();
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                mapa[by][bx] = ' ';
                bx += bxDir;
                by += byDir;
            }
            explosion(bx, by, 2, 9 * arma.nvl);
            return;
        }

        for (int b = 0; b < balas; b++) {
            int bx = px + bxDir;
            int by = py + byDir;
            int dano = 1 * arma.nvl;
            while (true) {
                if (mapa[by][bx] == '#') break;
                else if (mapa[by][bx] != ' ') {
                    explosion(bx, by, 0, dano);
                    break;
                } else {
                    mapa[by][bx] = 'o';
                    dibujar();
                    try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                    mapa[by][bx] = ' ';
                    bx += bxDir;
                    by += byDir;
                }
            }
        }
    }

    private boolean esCaminable(int cx, int cy) {
        char ch = mapa[cy][cx];
        return ch == ' ' || ch == '@';
    }

    public void moverEnemigos() {
        List<Enemigo> entidades = new ArrayList<>(enemigos);
        if (jefe != null) entidades.add(jefe);

        for (Enemigo ene : entidades) {
            int dx = Integer.compare(px, ene.x);
            int dy = Integer.compare(py, ene.y);

            boolean movio = false;
            int nx = ene.x;
            int ny = ene.y;

            if (dx != 0 && dy != 0) {
                if (random.nextBoolean()) {
                    if (esCaminable(ene.x + dx, ene.y)) { nx = ene.x + dx; ny = ene.y; movio = true; }
                    else if (esCaminable(ene.x, ene.y + dy)) { nx = ene.x; ny = ene.y + dy; movio = true; }
                } else {
                    if (esCaminable(ene.x, ene.y + dy)) { nx = ene.x; ny = ene.y + dy; movio = true; }
                    else if (esCaminable(ene.x + dx, ene.y)) { nx = ene.x + dx; ny = ene.y; movio = true; }
                }
            } else {
                if (dx != 0 && esCaminable(ene.x + dx, ene.y)) { nx = ene.x + dx; ny = ene.y; movio = true; }
                else if (dy != 0 && esCaminable(ene.x, ene.y + dy)) { nx = ene.x; ny = ene.y + dy; movio = true; }
            }

            if (movio) {
                char charDest = mapa[ny][nx];
                mapa[ene.y][ene.x] = ' ';
                ene.x = nx;
                ene.y = ny;

                if (charDest == '@') {
                    int dano = ene.hp;
                    if (armadura >= dano) {
                        armadura -= dano;
                        if (ene == jefe) {
                            msgAlerta = "¡Impacto con el Jefe! -" + dano + " Armadura.";
                            spawnJefe();
                            return;
                        } else {
                            msgAlerta = "¡Enemigo aplastado! -" + dano + " Armadura.";
                            enemigos.remove(ene);
                            enemigosRestantes--;
                        }
                    } else {
                        juegoPerdido = true;
                        mapa[ny][nx] = 'X';
                        return;
                    }
                } else {
                    mapa[ny][nx] = (ene == jefe) ? 'B' : ((ene.hp <= 9) ? (char) ('0' + ene.hp) : 'M');
                }
            }
        }
    }

    public void mainLoop() throws IOException {
        while (true) {
            screen.clear();
            tg.setForegroundColor(TextColor.ANSI.GREEN);
            tg.enableModifiers(SGR.BOLD);
            tg.putString(5, 2, "=== LABERINTOS DE MONSTRUOS ===");
            tg.disableModifiers(SGR.BOLD);

            tg.setForegroundColor(TextColor.ANSI.WHITE);
            tg.putString(5, 4, "Presiona ENTER para Iniciar");
            tg.putString(5, 5, "Presiona P para Salir");
            screen.refresh();

            KeyStroke k = screen.readInput();
            char kc = Character.toLowerCase(k.getCharacter() != null ? k.getCharacter() : ' ');
            if (kc == 'p' || k.getKeyType() == KeyType.Escape) break;
            if (k.getKeyType() != KeyType.Enter) continue;

            reiniciarStatsGlobales();

            while (!juegoPerdido && !juegoGanado) {
                msgAlerta = "";

                if (enemigosRestantes <= 0 && !faseJefe) {
                    faseJefe = true;
                    spawnJefe();
                } else if (faseJefe && jefe == null) {
                    if (multVida >= 15) {
                        juegoGanado = true;
                        continue;
                    } else {
                        multVida++;
                        enemigosMeta = (int) Math.round(enemigosMeta * 1.5);
                        msgAlerta = "¡JEFE DERROTADO! SUBES DE NIVEL (Meta: " + enemigosMeta + ")";
                        dibujar();
                        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                        iniciarNivel();
                        continue;
                    }
                }

                dibujar();

                KeyStroke t = screen.readInput();
                char c = Character.toLowerCase(t.getCharacter() != null ? t.getCharacter() : ' ');
                boolean turnoValido = false;

                if (c == 'p' || t.getKeyType() == KeyType.Escape) break;
                else if (c == ' ') armaActual = (armaActual < 4) ? armaActual + 1 : 1;
                else if (c == 't') tienda();
                else if (c == 'x' && granadas > 0) {
                    granadas--;
                    explosion(px, py, 1, 5);
                    turnoValido = true;
                } else if (c == 'b' && bombardeos > 0) {
                    bombardeos--;
                    if (armadura >= 8) {
                        armadura -= 8;
                        List<Enemigo> listaBombardeo = new ArrayList<>(enemigos);
                        if (jefe != null) listaBombardeo.add(jefe);
                        for (Enemigo ene : listaBombardeo) {
                            explosion(ene.x, ene.y, 0, 20);
                        }
                    } else {
                        juegoPerdido = true;
                        msgAlerta = "El bombardeo te destruyó.";
                    }
                    turnoValido = true;
                } else if (MOVIMIENTO.containsKey(c)) {
                    int[] dir = MOVIMIENTO.get(c);
                    if (mapa[py + dir[1]][px + dir[0]] == ' ') {
                        mapa[py][px] = ' ';
                        px += dir[0];
                        py += dir[1];
                        mapa[py][px] = '@';
                        turnoValido = true;
                    }
                } else if (DISPARO.containsKey(c)) {
                    int[] dir = DISPARO.get(c);
                    disparar(dir[0], dir[1]);
                    turnoValido = true;
                }

                if (turnoValido && !juegoPerdido && !juegoGanado) {
                    turnos++;
                    moverEnemigos();
                    spawnEnemigo();
                }
            }

            if (juegoPerdido || juegoGanado) {
                screen.clear();
                if (juegoGanado) {
                    tg.setForegroundColor(TextColor.ANSI.YELLOW);
                    tg.enableModifiers(SGR.BOLD);
                    tg.putString(10, 4, "¡VICTORIA SUPREMA!");
                    tg.disableModifiers(SGR.BOLD);
                    tg.setForegroundColor(TextColor.ANSI.WHITE);
                    tg.putString(10, 5, "Has superado el Nivel 15 y escapado del laberinto.");
                } else {
                    tg.setForegroundColor(TextColor.ANSI.RED);
                    tg.enableModifiers(SGR.BOLD);
                    tg.putString(10, 4, "¡HAS MUERTO!");
                    tg.disableModifiers(SGR.BOLD);
                }

                tg.setForegroundColor(TextColor.ANSI.WHITE);
                tg.putString(10, 7, "Puntaje Final: " + puntaje);
                tg.putString(10, 9, "Presiona cualquier tecla para volver al menú...");
                screen.refresh();
                screen.readInput();
            }
        }
        screen.stopScreen();
    }

    public static void main(String[] args) {
        try {
            new Bullet().mainLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}