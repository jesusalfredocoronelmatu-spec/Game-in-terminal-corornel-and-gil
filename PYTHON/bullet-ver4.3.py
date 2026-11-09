import curses
import random
import time

# --- CONFIGURACIÓN DE TECLAS ---
MOVIMIENTO = {
    'w': (0, -1), 's': (0, 1), 'a': (-1, 0), 'd': (1, 0),
    'q': (-1, -1), 'e': (1, -1), 'z': (-1, 1), 'c': (1, 1)
}
DISPARO = {
    'i': (0, -1), 'k': (0, 1), 'j': (-1, 0), 'l': (1, 0),
    'u': (-1, -1), 'o': (1, -1), 'n': (-1, 1), 'm': (1, 1)
}

class Juego:
    def __init__(self, stdscr):
        self.stdscr = stdscr
        curses.curs_set(0)
        self.stdscr.nodelay(0) # Juego por turnos
        
        # Configurar colores
        curses.start_color()
        curses.use_default_colors()
        curses.init_pair(1, curses.COLOR_CYAN, -1)   # Muros
        curses.init_pair(2, curses.COLOR_GREEN, -1)  # Jugador
        curses.init_pair(3, curses.COLOR_RED, -1)    # Enemigos
        curses.init_pair(4, curses.COLOR_MAGENTA, -1)# Jefe
        curses.init_pair(5, curses.COLOR_YELLOW, -1) # Explosiones
        curses.init_pair(6, curses.COLOR_WHITE, -1)  # Balas / Texto
        
        self.reiniciar_stats_globales()

    def reiniciar_stats_globales(self):
        self.puntaje = 0
        self.armadura = 1
        self.mult_vida = 1
        # Inventario: [Nivel, Municion]
        self.armas = {
            1: {"nombre": "Pistola", "nvl": 1, "muni": "Inf"},
            2: {"nombre": "Escopeta", "nvl": 1, "muni": 0},
            3: {"nombre": "Ráfaga", "nvl": 1, "muni": 0},
            4: {"nombre": "Cohetes", "nvl": 1, "muni": 0}
        }
        self.arma_actual = 1
        self.granadas = 0
        self.bombardeos = 0
        self.juego_ganado = False
        
        # Meta base de enemigos para el Nivel 1
        self.enemigos_meta = 10 
        self.iniciar_nivel()

    def iniciar_nivel(self):
        mapa_str = [
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
        ]
        self.mapa = [list(fila) for fila in mapa_str]
        self.py, self.px = 6, 19
        self.enemigos = []
        self.enemigos_restantes = self.enemigos_meta # Asigna la meta actual del nivel
        self.jefe = None
        self.fase_jefe = False 
        self.turnos = 0
        self.juego_perdido = False
        self.msg_alerta = ""

    def dibujar(self):
        self.stdscr.clear()
        arma = self.armas[self.arma_actual]
        # HUD
        titulo = f"=== LABERINTOS DE MONSTRUOS | NIVEL x{self.mult_vida} ===" if not self.fase_jefe else f"=== ¡BATALLA CONTRA EL JEFE! (NIVEL x{self.mult_vida}) ==="
        self.stdscr.addstr(0, 0, titulo, curses.color_pair(6) | curses.A_BOLD)
        self.stdscr.addstr(1, 0, f"PUNTAJE: {self.puntaje:05d} | Restantes para Jefe: {max(0, self.enemigos_restantes)}", curses.color_pair(6))
        self.stdscr.addstr(2, 0, f"Arma: {arma['nombre']} Nv.{arma['nvl']} ({arma['muni']}) | Armadura: {self.armadura}", curses.color_pair(2))
        self.stdscr.addstr(3, 0, f"Granadas[X]: {self.granadas} | Bombardeo[B]: {self.bombardeos} | Cambiar[Espacio]", curses.color_pair(5))
        
        # NUEVA LÍNEA DE CONTROLES
        self.stdscr.addstr(4, 0, "Mover: recto:WASD/diagonal:QEZC | Disparar: recto:IJKL/diagonal:UONM | Tienda: [T]", curses.color_pair(6))
        
        # Alertas o separador desplazados a la línea 5
        if self.msg_alerta:
            self.stdscr.addstr(5, 0, f">> {self.msg_alerta} <<", curses.color_pair(5) | curses.A_BOLD)
        else:
            self.stdscr.addstr(5, 0, "-" * 75)

        # Mapa
        for y, fila in enumerate(self.mapa):
            for x, char in enumerate(fila):
                color = curses.color_pair(6)
                if char == '#': color = curses.color_pair(1)
                elif char == '@': color = curses.color_pair(2) | curses.A_BOLD
                elif char == 'B': color = curses.color_pair(4) | curses.A_BOLD
                elif char.isdigit() or char == 'M': color = curses.color_pair(3)
                elif char == '*' or char == 'R': color = curses.color_pair(5) | curses.A_BOLD
                
                try:
                    self.stdscr.addstr(y + 5, x, char, color)
                except curses.error:
                    pass
        self.stdscr.refresh()

    def spawn_enemigo(self):
        if self.fase_jefe or self.enemigos_restantes <= 0: return
        
        prob_actual = min(100, 15 + (self.turnos // 10) * 5)
        if random.randint(1, 100) > prob_actual: return
        
        if len(self.enemigos) >= 40: return
        
        while True:
            rx, ry = random.randint(1, 38), random.randint(1, 13)
            if self.mapa[ry][rx] == ' ' and abs(rx - self.px) + abs(ry - self.py) > 3:
                hp = random.randint(1, 4) * self.mult_vida
                self.enemigos.append({"x": rx, "y": ry, "hp": hp})
                self.mapa[ry][rx] = str(hp) if hp <= 9 else 'M'
                break

    def spawn_jefe(self):
        self.msg_alerta = "¡HA APARECIDO EL JEFE!"
        while True:
            rx, ry = random.randint(1, 38), random.randint(1, 13)
            if self.mapa[ry][rx] == ' ' and abs(rx - self.px) + abs(ry - self.py) > 5:
                self.jefe = {"x": rx, "y": ry, "hp": 7 * self.mult_vida}
                self.mapa[ry][rx] = 'B'
                break

    def explosion(self, cx, cy, radio, dano):
        self.msg_alerta = "¡BOOM!"
        afectados = []
        for dy in range(-radio, radio + 1):
            for dx in range(-radio, radio + 1):
                nx, ny = cx + dx, cy + dy
                if 1 <= ny <= 13 and 1 <= nx <= 38 and self.mapa[ny][nx] != '#':
                    afectados.append((nx, ny))
                    self.mapa[ny][nx] = '*'
        
        afectados = list(set(afectados))
        
        self.dibujar()
        time.sleep(0.15)
        
        for nx, ny in afectados:
            self.mapa[ny][nx] = ' '
            
            for ene in self.enemigos[:]:
                if ene['x'] == nx and ene['y'] == ny:
                    ene['hp'] -= dano
                    self.puntaje += 10
                    if ene['hp'] <= 0:
                        if ene in self.enemigos: self.enemigos.remove(ene)
                        self.puntaje += 50
                        self.enemigos_restantes -= 1
                    else:
                        self.mapa[ny][nx] = str(ene['hp']) if ene['hp'] <= 9 else 'M'
                        
            if self.jefe and self.jefe['x'] == nx and self.jefe['y'] == ny:
                self.jefe['hp'] -= dano
                self.puntaje += 20
                if self.jefe['hp'] <= 0:
                    self.jefe = None 
                    self.puntaje += 200
                else:
                    self.mapa[ny][nx] = 'B'
                    
        self.mapa[self.py][self.px] = '@'

    def tienda(self):
        secuencia_truco = "" 
        
        while True:
            self.stdscr.clear()
            self.stdscr.addstr(0, 0, "=== TIENDA DE ARSENAL ===", curses.color_pair(5) | curses.A_BOLD)
            self.stdscr.addstr(1, 0, f"Puntos: {self.puntaje}", curses.color_pair(6))
            opciones = [
                f"[1] Mejorar Armadura Nvl {self.armadura + 1} ({1000 * self.armadura} pts)",
                f"[2] Escopeta +10 Balas      (600 pts)  Tienes: {self.armas[2]['muni']}",
                f"[3] Ráfaga +30 Balas        (800 pts)  Tienes: {self.armas[3]['muni']}",
                f"[4] Cohetes +1 Misil        (3000 pts) Tienes: {self.armas[4]['muni']}",
                f"[5] Granadas +3             (500 pts)  Tienes: {self.granadas}",
                f"[6] Bombardeo +1            (20000 pts)Tienes: {self.bombardeos}",
                f"[7] Mejorar Pistola Nvl {self.armas[1]['nvl']+1}  ({500 * self.armas[1]['nvl']} pts)",
                f"[8] Mejorar Escopeta Nvl {self.armas[2]['nvl']+1} ({800 * self.armas[2]['nvl']} pts)",
                f"[9] Mejorar Ráfaga Nvl {self.armas[3]['nvl']+1}   ({800 * self.armas[3]['nvl']} pts)",
                f"[0] Mejorar Cohetes Nvl {self.armas[4]['nvl']+1}  ({1000 * self.armas[4]['nvl']} pts)",
                "Presiona P o ESC para salir."
            ]
            for i, op in enumerate(opciones):
                self.stdscr.addstr(3 + i, 0, op)
            self.stdscr.refresh()
            
            t = self.stdscr.getch()
            c = chr(t).lower() if t < 256 else ""
            
            # --- TRUCO SECRETO ---
            if c.isalpha(): 
                secuencia_truco += c
                if len(secuencia_truco) > 10:
                    secuencia_truco = secuencia_truco[-10:] 
                
                if secuencia_truco == "wwssadadba":
                    self.puntaje += 100000000
                    secuencia_truco = "" 
                    continue 
            
            if c == 'p' or t == 27: break
            
            costos = {
                '1': (1000 * self.armadura, lambda: setattr(self, 'armadura', self.armadura + 1)),
                '2': (600, lambda: self.armas[2].update({'muni': self.armas[2]['muni'] + 10})),
                '3': (800, lambda: self.armas[3].update({'muni': self.armas[3]['muni'] + 30})),
                '4': (3000, lambda: self.armas[4].update({'muni': self.armas[4]['muni'] + 1})),
                '5': (500, lambda: setattr(self, 'granadas', self.granadas + 3)),
                '6': (20000, lambda: setattr(self, 'bombardeos', self.bombardeos + 1)),
                '7': (500 * self.armas[1]['nvl'], lambda: self.armas[1].update({'nvl': self.armas[1]['nvl'] + 1})),
                '8': (800 * self.armas[2]['nvl'], lambda: self.armas[2].update({'nvl': self.armas[2]['nvl'] + 1})),
                '9': (800 * self.armas[3]['nvl'], lambda: self.armas[3].update({'nvl': self.armas[3]['nvl'] + 1})),
                '0': (1000 * self.armas[4]['nvl'], lambda: self.armas[4].update({'nvl': self.armas[4]['nvl'] + 1}))
            }
            if c in costos and self.puntaje >= costos[c][0]:
                self.puntaje -= costos[c][0]
                costos[c][1]()

    def disparar(self, bx_dir, by_dir):
        arma = self.armas[self.arma_actual]
        if self.arma_actual != 1 and arma['muni'] <= 0:
            self.msg_alerta = "¡Sin munición!"
            return

        balas = 1 if self.arma_actual in [1, 4] else (3 if self.arma_actual == 3 else 1)
        if self.arma_actual == 3: arma['muni'] -= 3
        elif self.arma_actual in [2, 4]: arma['muni'] -= 1

        if self.arma_actual == 2: # Escopeta
            self.msg_alerta = "¡Boom de Escopeta!"
            dano = 2 * arma['nvl']
            afectados = []
            
            for d in range(1, 4):
                for w in [-1, 0, 1]:
                    nx = self.px + (d * bx_dir) + (w if by_dir != 0 else 0)
                    ny = self.py + (d * by_dir) + (w if bx_dir != 0 else 0)
                    if 1 <= ny <= 13 and 1 <= nx <= 38 and self.mapa[ny][nx] != '#':
                        afectados.append((nx, ny))
                        self.mapa[ny][nx] = '*'
                        
            afectados = list(set(afectados))
            self.dibujar()
            time.sleep(0.1)
            
            for nx, ny in afectados:
                self.mapa[ny][nx] = ' '
                
                for ene in self.enemigos[:]:
                    if ene['x'] == nx and ene['y'] == ny:
                        ene['hp'] -= dano
                        self.puntaje += 10
                        if ene['hp'] <= 0:
                            if ene in self.enemigos: self.enemigos.remove(ene)
                            self.puntaje += 50
                            self.enemigos_restantes -= 1
                        else:
                            self.mapa[ny][nx] = str(ene['hp']) if ene['hp'] <= 9 else 'M'
                            
                if self.jefe and self.jefe['x'] == nx and self.jefe['y'] == ny:
                    self.jefe['hp'] -= dano
                    self.puntaje += 20
                    if self.jefe['hp'] <= 0:
                        self.jefe = None
                        self.puntaje += 200
                    else:
                        self.mapa[ny][nx] = 'B'
                        
            self.mapa[self.py][self.px] = '@'
            return
            
        elif self.arma_actual == 4: # Cohete
            bx, by = self.px + bx_dir, self.py + by_dir
            while self.mapa[by][bx] == ' ':
                self.mapa[by][bx] = 'R'
                self.dibujar()
                time.sleep(0.03)
                self.mapa[by][bx] = ' '
                bx, by = bx + bx_dir, by + by_dir
            self.explosion(bx, by, 2, 9 * arma['nvl'])
            return

        # Pistola y Ráfaga
        for b in range(balas):
            bx, by = self.px + bx_dir, self.py + by_dir
            dano = 1 * arma['nvl']
            while True:
                if self.mapa[by][bx] == '#': break
                elif self.mapa[by][bx] != ' ':
                    self.explosion(bx, by, 0, dano)
                    break
                else:
                    self.mapa[by][bx] = 'o'
                    self.dibujar()
                    time.sleep(0.02)
                    self.mapa[by][bx] = ' '
                    bx, by = bx + bx_dir, by + by_dir

    def mover_enemigos(self):
        entidades = self.enemigos + ([self.jefe] if self.jefe else [])
        for ene in entidades:
            dx = 1 if self.px > ene['x'] else (-1 if self.px < ene['x'] else 0)
            dy = 1 if self.py > ene['y'] else (-1 if self.py < ene['y'] else 0)
            
            def es_caminable(cx, cy):
                return self.mapa[cy][cx] in [' ', '@']
            
            movio = False
            nx, ny = ene['x'], ene['y']
            
            if dx != 0 and dy != 0:
                if random.choice([True, False]):
                    if es_caminable(ene['x'] + dx, ene['y']): nx, ny = ene['x'] + dx, ene['y']; movio = True
                    elif es_caminable(ene['x'], ene['y'] + dy): nx, ny = ene['x'], ene['y'] + dy; movio = True
                else:
                    if es_caminable(ene['x'], ene['y'] + dy): nx, ny = ene['x'], ene['y'] + dy; movio = True
                    elif es_caminable(ene['x'] + dx, ene['y']): nx, ny = ene['x'] + dx, ene['y']; movio = True
            else:
                if dx != 0 and es_caminable(ene['x'] + dx, ene['y']): nx, ny = ene['x'] + dx, ene['y']; movio = True
                elif dy != 0 and es_caminable(ene['x'], ene['y'] + dy): nx, ny = ene['x'], ene['y'] + dy; movio = True

            if movio:
                char_dest = self.mapa[ny][nx]
                self.mapa[ene['y']][ene['x']] = ' '
                ene['x'], ene['y'] = nx, ny
                
                if char_dest == '@':
                    dano = ene['hp']
                    if self.armadura >= dano:
                        self.armadura -= dano
                        self.msg_alerta = f"¡Enemigo aplastado! -{dano} Armadura."
                        if ene in self.enemigos:
                            self.enemigos.remove(ene)
                            self.enemigos_restantes -= 1
                        else:
                            self.msg_alerta = f"¡Impacto con el Jefe! -{dano} Armadura."
                            self.spawn_jefe()
                            return 
                    else:
                        self.juego_perdido = True
                        self.mapa[ny][nx] = 'X'
                        return
                else:
                    self.mapa[ny][nx] = 'B' if ene == self.jefe else (str(ene['hp']) if ene['hp'] <= 9 else 'M')

    def main_loop(self):
        while True:
            self.stdscr.clear()
            self.stdscr.addstr(2, 5, "=== LABERINTOS DE MONSTRUOS ===", curses.color_pair(2) | curses.A_BOLD)
            self.stdscr.addstr(4, 5, "Presiona ENTER para Iniciar")
            self.stdscr.addstr(5, 5, "Presiona P para Salir")
            self.stdscr.refresh()
            k = self.stdscr.getch()
            if k in [ord('p'), ord('P'), 27]: break
            if k not in [10, 13]: continue

            self.reiniciar_stats_globales()
            
            while not self.juego_perdido and not self.juego_ganado:
                self.msg_alerta = ""
                
                if self.enemigos_restantes <= 0 and not self.fase_jefe:
                    self.fase_jefe = True
                    self.spawn_jefe()
                
                elif self.fase_jefe and self.jefe is None: 
                    if self.mult_vida >= 15:
                        self.juego_ganado = True
                        continue
                    else:
                        self.mult_vida += 1
                        # --- ESCALADO DE ENEMIGOS POR X 1.5 REDONDEADO ---
                        self.enemigos_meta = round(self.enemigos_meta * 1.5)
                        self.msg_alerta = f"¡JEFE DERROTADO! SUBES DE NIVEL (Meta: {self.enemigos_meta})"
                        self.dibujar()
                        time.sleep(1.5)
                        self.iniciar_nivel()
                        continue

                self.dibujar()
                
                # ==========================================
                # CORRECCIÓN: Limpiar el buffer de teclado
                # ==========================================
                curses.flushinp()
                
                t = self.stdscr.getch()
                c = chr(t).lower() if t < 256 else ""
                
                turno_valido = False

                if c == 'p' or t == 27: break
                elif c == ' ': self.arma_actual = self.arma_actual + 1 if self.arma_actual < 4 else 1
                elif c == 't': self.tienda()
                elif c == 'x' and self.granadas > 0:
                    self.granadas -= 1
                    self.explosion(self.px, self.py, 1, 5)
                    turno_valido = True
                elif c == 'b' and self.bombardeos > 0:
                    self.bombardeos -= 1
                    if self.armadura >= 8:
                        self.armadura -= 8
                        for ene in self.enemigos[:] + ([self.jefe] if self.jefe else []):
                            self.explosion(ene['x'], ene['y'], 0, 20)
                    else:
                        self.juego_perdido = True
                        self.msg_alerta = "El bombardeo te destruyó."
                    turno_valido = True
                elif c in MOVIMIENTO:
                    dx, dy = MOVIMIENTO[c]
                    if self.mapa[self.py + dy][self.px + dx] == ' ':
                        self.mapa[self.py][self.px] = ' '
                        self.px += dx
                        self.py += dy
                        self.mapa[self.py][self.px] = '@'
                        turno_valido = True
                elif c in DISPARO:
                    self.disparar(DISPARO[c][0], DISPARO[c][1])
                    turno_valido = True

                if turno_valido and not self.juego_perdido and not self.juego_ganado:
                    self.turnos += 1
                    self.mover_enemigos()
                    self.spawn_enemigo()

            if self.juego_perdido or self.juego_ganado:
                self.stdscr.clear()
                if self.juego_ganado:
                    self.stdscr.addstr(4, 10, "¡VICTORIA SUPREMA!", curses.color_pair(5) | curses.A_BOLD)
                    self.stdscr.addstr(5, 10, "Has superado el Nivel 15 y escapado del laberinto.")
                else:
                    self.stdscr.addstr(4, 10, "¡HAS MUERTO!", curses.color_pair(3) | curses.A_BOLD)
                    
                self.stdscr.addstr(7, 10, f"Puntaje Final: {self.puntaje}")
                self.stdscr.addstr(9, 10, "Presiona cualquier tecla para volver al menú...")
                self.stdscr.refresh()
                self.stdscr.getch()

if __name__ == "__main__":
    curses.wrapper(lambda stdscr: Juego(stdscr).main_loop())