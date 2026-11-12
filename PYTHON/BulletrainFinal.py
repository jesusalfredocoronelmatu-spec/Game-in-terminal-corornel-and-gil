import os
import random
import sys


def modificar_cadena(cadena_orig, pos, nuevo_c):
    idx = pos - 1
    if idx < 0 or idx >= len(cadena_orig):
        return cadena_orig
    return cadena_orig[:idx] + nuevo_c + cadena_orig[idx + 1 :]


def borrar_pantalla():
    os.system("cls" if os.name == "nt" else "clear")


def mostrar_instrucciones():
    borrar_pantalla()
    print("==================================================")
    print("           MANUAL DE CONTROLES DEL JUEGO          ")
    print("==================================================")
    print("  MOVIMIENTO:")
    print("  - Arriba    [ W ]  ( ^ )          - Arriba-Izq [ Q ]")
    print("  - Abajo     [ S ]  ( v )          - Arriba-Der [ E ]")
    print("  - Izquierda [ A ]  ( < )          - Abajo-Izq  [ Z ]")
    print("  - Derecha   [ D ]  ( > )          - Abajo-Der  [ C ]")
    print("")
    print("  DISPARO:")
    print("  - Arriba    [ I ]  ( ^ )          - Arriba-Izq [ U ]")
    print("  - Abajo     [ K ]  ( v )          - Arriba-Der [ O ]")
    print("  - Izquierda [ J ]  ( < )          - Abajo-Izq  [ N ]")
    print("  - Derecha   [ L ]  ( > )          - Abajo-Der  [ M ]")
    print("")
    print("  ACCIONES Y ESPECIALES:")
    print("  - Cambiar Arma: [ ESPACIO ]       - Tienda: [ T ]")
    print("  - Granada:      [ X ]             - Bombardeo: [ B ]")
    print("==================================================")
    input("        [ Presiona ENTER para regresar ]")


def mostrar_menu_principal():
    borrar_pantalla()
    print("==================================================")
    print("        LABERINTOS DE MONSTRUOS: ARSENAL TOTAL    ")
    print("==================================================")
    print("             [ Presiona ENTER para INICIAR ]      ")
    print("             [ Presiona H   para ver el MANUAL ]  ")
    print("             [ Presiona P   para SALIR ]          ")
    print("==================================================")


def main():
    juego_terminado = False

    while not juego_terminado:
        en_menu = True
        while en_menu and not juego_terminado:
            mostrar_menu_principal()
            tecla = input().strip().lower()
            if tecla == "":
                en_menu = False
            elif tecla == "h":
                mostrar_instrucciones()
            elif tecla == "p" or tecla == "esc":
                juego_terminado = True
                en_menu = False

        if not juego_terminado:
            mapa = [
                "",
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
                "########################################",
            ]
            filas = 15

            max_enemigos = 40
            ene_x = [0] * 41
            ene_y = [0] * 41
            ene_hp = [0] * 41
            ene_activo = [False] * 41

            juego_perdido = False
            puntaje = 0
            turnos_jugados = 0
            prob_actual = 15
            mult_vida = 1
            enemigos_restantes = 10
            fase_boss = False
            boss_activo = False

            arma_actual = 1
            armadura = 1
            muni_escopeta = 0
            muni_rafaga = 0
            muni_cohetes = 0
            granadas = 0
            bombardeos = 0
            nvl_pistola = 1
            nvl_escopeta = 1
            nvl_rafaga = 1
            nvl_cohetes = 1
            last_fx = 1
            last_fy = 0

            # Encontrar al jugador
            px, py = 1, 1
            for i in range(1, filas + 1):
                for j in range(1, len(mapa[i]) + 1):
                    char_p = mapa[i][j - 1]
                    if char_p == "@" or char_p == "+":
                        py = i
                        px = j

            reiniciar_nivel = False

            while not reiniciar_nivel and not juego_terminado:
                borrar_pantalla()

                if arma_actual == 1:
                    nombre_arma = f"Pistola Nv.{nvl_pistola} (Infinito)"
                elif arma_actual == 2:
                    nombre_arma = f"Escopeta Nv.{nvl_escopeta} ({muni_escopeta})"
                elif arma_actual == 3:
                    nombre_arma = f"Rafaga Nv.{nvl_rafaga} ({muni_rafaga})"
                else:
                    nombre_arma = f"Cohetes Nv.{nvl_cohetes} ({muni_cohetes})"

                if fase_boss:
                    print(
                        f"=== LABERINTOS DE MONSTRUOS | ¡BATALLA CONTRA EL JEFE! (NIVEL {mult_vida} DE 15) ==="
                    )
                    print(f"PUNTAJE: {puntaje} | Vida del Jefe: {boss_hp}")
                else:
                    print(
                        f"=== LABERINTOS DE MONSTRUOS | NIVEL (VIDA/DAÑO ENEMIGO): x{mult_vida} DE 15 ==="
                    )
                    print(
                        f"PUNTAJE: {puntaje} | Monstruos para el Jefe: {enemigos_restantes}"
                    )

                print(
                    "-----------------------------------------------------------------"
                )
                print(f"Arma: {nombre_arma} | Armadura: {armadura}")
                print(
                    f"Granadas[X]: {granadas} | Bombardeo[B]: {bombardeos} | Cambiar Arma: [Espacio]"
                )
                print(
                    "-----------------------------------------------------------------"
                )

                for i in range(1, filas + 1):
                    print(mapa[i])

                if juego_perdido:
                    print(
                        "-----------------------------------------------------------------"
                    )
                    print("¡HAS MUERTO! Fin del Juego.")
                    print(f"Tu puntaje final es: {puntaje}")
                    input(
                        "Presiona [ENTER] para salir al menú o reintentar..."
                    )
                    reiniciar_nivel = True
                else:
                    tecla = input("Tu accion + ENTER: ").strip().lower()
                    turno_valido = False
                    dx, dy = 0, 0
                    bx_dir, by_dir = 0, 0

                    if tecla == "wwssadadba":
                        print(
                            "=================================================="
                        )
                        print(
                            "            ¡CÓDIGO SECRETO ACTIVADO!             "
                        )
                        print(
                            "=================================================="
                        )
                        try:
                            puntaje = int(
                                input("¿Cuántos puntos deseas tener? ")
                            )
                        except ValueError:
                            puntaje = 9999
                    elif tecla in ["p", "esc", "r"]:
                        reiniciar_nivel = True
                    elif tecla == "" or tecla == " ":
                        arma_actual += 1
                        if arma_actual > 4:
                            arma_actual = 1
                    elif tecla == "t":
                        en_tienda = True
                        while en_tienda:
                            borrar_pantalla()
                            print(
                                "=================================================="
                            )
                            print(
                                "                 TIENDA DE ARSENAL                "
                            )
                            print(
                                "=================================================="
                            )
                            print(f"Puntos: {puntaje}")
                            print(
                                f" [1] Mejorar Armadura Nvl {armadura + 1} ({1000 * armadura} pts)"
                            )
                            print(
                                f" [2] Escopeta +10 Balas     (600 pts)   Tienes: {muni_escopeta}"
                            )
                            print(
                                f" [3] Fusil Rafaga +30 Balas (800 pts)   Tienes: {muni_rafaga}"
                            )
                            print(
                                f" [4] Cohetes +1 Misil       (3000 pts)  Tienes: {muni_cohetes}"
                            )
                            print(
                                f" [5] Granadas +3            (500 pts)   Tienes: {granadas}"
                            )
                            print(
                                f" [6] Bombardeo Aereo +1     (20000 pts) Tienes: {bombardeos}"
                            )
                            print(" --- MEJORAS DE DAÑO ---")
                            print(
                                f" [7] Mejorar Pistola Nvl {nvl_pistola + 1}  ({500 * nvl_pistola} pts)"
                            )
                            print(
                                f" [8] Mejorar Escopeta Nvl {nvl_escopeta + 1} ({800 * nvl_escopeta} pts)"
                            )
                            print(
                                f" [9] Mejorar Fusil Ráfaga Nvl {nvl_rafaga + 1} ({800 * nvl_rafaga} pts)"
                            )
                            print(
                                f" [0] Mejorar Cohetes Nvl {nvl_cohetes + 1}  ({1000 * nvl_cohetes} pts)"
                            )
                            print(" [P] Salir de la Tienda")
                            print(
                                "=================================================="
                            )
                            op_tienda = input("Selección: ").strip().lower()

                            if op_tienda in ["p", "esc"]:
                                en_tienda = False
                            else:
                                if op_tienda == "1" and puntaje >= (
                                    1000 * armadura
                                ):
                                    puntaje -= 1000 * armadura
                                    armadura += 1
                                elif op_tienda == "2" and puntaje >= 600:
                                    puntaje -= 600
                                    muni_escopeta += 10
                                elif op_tienda == "3" and puntaje >= 800:
                                    puntaje -= 800
                                    muni_rafaga += 30
                                elif op_tienda == "4" and puntaje >= 3000:
                                    puntaje -= 3000
                                    muni_cohetes += 1
                                elif op_tienda == "5" and puntaje >= 500:
                                    puntaje -= 500
                                    granadas += 3
                                elif op_tienda == "6" and puntaje >= 20000:
                                    puntaje -= 20000
                                    bombardeos += 1
                                elif op_tienda == "7" and puntaje >= (
                                    500 * nvl_pistola
                                ):
                                    puntaje -= 500 * nvl_pistola
                                    nvl_pistola += 1
                                elif op_tienda == "8" and puntaje >= (
                                    800 * nvl_escopeta
                                ):
                                    puntaje -= 800 * nvl_escopeta
                                    nvl_escopeta += 1
                                elif op_tienda == "9" and puntaje >= (
                                    800 * nvl_rafaga
                                ):
                                    puntaje -= 800 * nvl_rafaga
                                    nvl_rafaga += 1
                                elif op_tienda == "0" and puntaje >= (
                                    1000 * nvl_cohetes
                                ):
                                    puntaje -= 1000 * nvl_cohetes
                                    nvl_cohetes += 1
                    else:
                        if tecla == "w":
                            dy, turno_valido = -1, True
                        elif tecla == "s":
                            dy, turno_valido = 1, True
                        elif tecla == "a":
                            dx, turno_valido = -1, True
                        elif tecla == "d":
                            dx, turno_valido = 1, True
                        elif tecla == "q":
                            dy, dx, turno_valido = -1, -1, True
                        elif tecla == "e":
                            dy, dx, turno_valido = -1, 1, True
                        elif tecla == "z":
                            dy, dx, turno_valido = 1, -1, True
                        elif tecla == "c":
                            dy, dx, turno_valido = 1, 1, True

                        elif tecla == "i":
                            by_dir, bx_dir, turno_valido = -1, 0, True
                        elif tecla == "k":
                            by_dir, bx_dir, turno_valido = 1, 0, True
                        elif tecla == "j":
                            bx_dir, by_dir, turno_valido = -1, 0, True
                        elif tecla == "l":
                            bx_dir, by_dir, turno_valido = 1, 0, True
                        elif tecla == "u":
                            by_dir, bx_dir, turno_valido = -1, -1, True
                        elif tecla == "o":
                            by_dir, bx_dir, turno_valido = -1, 1, True
                        elif tecla == "n":
                            by_dir, bx_dir, turno_valido = 1, -1, True
                        elif tecla == "m":
                            by_dir, bx_dir, turno_valido = 1, 1, True

                        if turno_valido:
                            last_fx = dx if dx != 0 else bx_dir
                            last_fy = dy if dy != 0 else by_dir

                    if not reiniciar_nivel and not juego_perdido:
                        if dx != 0 or dy != 0:
                            tx, ty = px + dx, py + dy
                            if (
                                1 <= ty <= filas
                                and 1 <= tx <= len(mapa[1])
                            ):
                                char_p = mapa[py][px - 1]
                                char_t = mapa[ty][tx - 1]
                                char_p_new = " " if char_p == "@" else "."

                                if char_t == " " or char_t == ".":
                                    char_t_new = "@" if char_t == " " else "+"
                                    mapa[py] = modificar_cadena(
                                        mapa[py], px, char_p_new
                                    )
                                    mapa[ty] = modificar_cadena(
                                        mapa[ty], tx, char_t_new
                                    )
                                    py, px = ty, tx

                        if bx_dir != 0 or by_dir != 0:
                            turno_valido = True
                            b_x, b_y = px + bx_dir, py + by_dir
                            bala_activa = True

                            while (
                                bala_activa
                                and 1 <= b_y <= filas
                                and 1 <= b_x <= len(mapa[1])
                            ):
                                char_b = mapa[b_y][b_x - 1]
                                if char_b == "#":
                                    bala_activa = False
                                elif (
                                    char_b != " "
                                    and char_b != "."
                                    and char_b != "o"
                                ):
                                    for k in range(1, max_enemigos + 1):
                                        if (
                                            ene_activo[k]
                                            and ene_x[k] == b_x
                                            and ene_y[k] == b_y
                                        ):
                                            ene_hp[k] -= nvl_pistola
                                            puntaje += 10
                                            if ene_hp[k] <= 0:
                                                ene_activo[k] = False
                                                puntaje += 50
                                                mapa[b_y] = modificar_cadena(
                                                    mapa[b_y], b_x, " "
                                                )
                                                if not fase_boss:
                                                    enemigos_restantes -= 1
                                            else:
                                                c_vida = (
                                                    str(ene_hp[k])
                                                    if ene_hp[k] <= 9
                                                    else "M"
                                                )
                                                mapa[b_y] = modificar_cadena(
                                                    mapa[b_y], b_x, c_vida
                                                )
                                    bala_activa = False
                                else:
                                    mapa[b_y] = modificar_cadena(
                                        mapa[b_y], b_x, "o"
                                    )
                                    b_x += bx_dir
                                    b_y += by_dir

                        if enemigos_restantes <= 0 and not fase_boss:
                            fase_boss = True
                            boss_activo = True
                            boss_hp = 7 * mult_vida
                            boss_x, boss_y = 5, 5
                            mapa[boss_y] = modificar_cadena(
                                mapa[boss_y], boss_x, "B"
                            )

                        if turno_valido and not juego_perdido:
                            turnos_jugados += 1
                            if not fase_boss:
                                if turnos_jugados % 10 == 0:
                                    prob_actual += 5
                                    if prob_actual > 100:
                                        prob_actual = 100

                                if random.randint(1, 100) <= prob_actual:
                                    slot_libre = 0
                                    k = 1
                                    while (
                                        k <= max_enemigos and slot_libre == 0
                                    ):
                                        if not ene_activo[k]:
                                            slot_libre = k
                                        k += 1

                                    if slot_libre > 0:
                                        spot_valido = False
                                        while not spot_valido:
                                            ry = random.randint(1, filas)
                                            rx = random.randint(
                                                1, len(mapa[1])
                                            )
                                            if (
                                                mapa[ry][rx - 1] == " "
                                                and (
                                                    abs(rx - px)
                                                    + abs(ry - py)
                                                )
                                                > 3
                                            ):
                                                spot_valido = True

                                        ene_x[slot_libre] = rx
                                        ene_y[slot_libre] = ry
                                        vida_base = random.randint(1, 4)
                                        ene_hp[slot_libre] = (
                                            vida_base * mult_vida
                                        )
                                        ene_activo[slot_libre] = True
                                        c_vida = (
                                            str(ene_hp[slot_libre])
                                            if ene_hp[slot_libre] <= 9
                                            else "M"
                                        )
                                        mapa[ry] = modificar_cadena(
                                            mapa[ry], rx, c_vida
                                        )

                            for k in range(1, max_enemigos + 1):
                                if ene_activo[k]:
                                    dx_e = (
                                        -1
                                        if px < ene_x[k]
                                        else (1 if px > ene_x[k] else 0)
                                    )
                                    dy_e = (
                                        -1
                                        if py < ene_y[k]
                                        else (1 if py > ene_y[k] else 0)
                                    )

                                    movio = False
                                    nex, ney = ene_x[k], ene_y[k]

                                    if dx_e != 0 and (
                                        1
                                        <= ene_x[k] + dx_e
                                        <= len(mapa[1])
                                    ):
                                        c_t = mapa[ene_y[k]][
                                            ene_x[k] + dx_e - 1
                                        ]
                                        if c_t in [" ", "@", "+"]:
                                            nex, ney = ene_x[k] + dx_e, ene_y[k]
                                            movio = True
                                    elif dy_e != 0 and (
                                        1 <= ene_y[k] + dy_e <= filas
                                    ):
                                        c_t = mapa[ene_y[k] + dy_e][
                                            ene_x[k] - 1
                                        ]
                                        if c_t in [" ", "@", "+"]:
                                            nex, ney = ene_x[k], ene_y[k] + dy_e
                                            movio = True

                                    if movio:
                                        mapa[ene_y[k]] = modificar_cadena(
                                            mapa[ene_y[k]], ene_x[k], " "
                                        )
                                        c_t = mapa[ney][nex - 1]
                                        ene_x[k], ene_y[k] = nex, ney

                                        if c_t in ["@", "+"]:
                                            dano_recibido = ene_hp[k]
                                            if armadura >= dano_recibido:
                                                armadura -= dano_recibido
                                                ene_activo[k] = False
                                                puntaje += 50
                                                if not fase_boss:
                                                    enemigos_restantes -= 1
                                            else:
                                                juego_perdido = True
                                        else:
                                            c_vida = (
                                                str(ene_hp[k])
                                                if ene_hp[k] <= 9
                                                else "M"
                                            )
                                            mapa[ney] = modificar_cadena(
                                                mapa[ney], nex, c_vida
                                            )

                        if fase_boss and not boss_activo:
                            if mult_vida >= 15:
                                borrar_pantalla()
                                print(
                                    "¡VICTORIA SUPREMA! Has superado el nivel 15."
                                )
                                input()
                                reiniciar_nivel = True
                            else:
                                mult_vida += 1
                                enemigos_restantes = 10
                                fase_boss = False

    borrar_pantalla()
    print("Has salido del laberinto. ¡Hasta pronto!")


if __name__ == "__main__":
    main()