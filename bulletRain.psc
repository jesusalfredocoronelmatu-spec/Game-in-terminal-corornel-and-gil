SubProceso res = ModificarCadena(cadena_orig, pos, nuevo_c)
    Definir izq, der, res Como Caracter
    Si pos > 1 Entonces
        izq = Subcadena(cadena_orig, 1, pos - 1)
    Sino
        izq = ""
    FinSi
    Si pos < Longitud(cadena_orig) Entonces
        der = Subcadena(cadena_orig, pos + 1, Longitud(cadena_orig))
    Sino
        der = ""
    FinSi
    res = izq + nuevo_c + der
FinSubProceso

Algoritmo odioEsto
    Definir mapa Como Caracter
    Dimension mapa[40]
    Definir filas, px, py Como Entero
    Definir juego_terminado, reiniciar_nivel, juego_perdido Como Logico
    Definir tecla, char_p, char_t, char_b, char_p_new, char_t_new, char_b_new, c_t Como Caracter
    Definir dx, dy, tx, ty, bx, by, i, j Como Entero
    Definir turno_valido, movio, spot_valido, bala_activa Como Logico
    
    // Variables de Enemigos y Disparos
    Definir max_enemigos, k, slot_libre, rx, ry Como Entero
    Definir bx_dir, by_dir, b_x, b_y, dx_e, dy_e, nex, ney Como Entero
    max_enemigos = 40 
    Dimension ene_x[40], ene_y[40], ene_hp[40], ene_activo[40]
    Definir ene_x, ene_y, ene_hp Como Entero
    Definir ene_activo Como Logico
    
    // Variables de Puntaje y Probabilidad
    Definir puntaje, turnos_jugados, prob_actual Como Entero
    
    juego_terminado = Falso
    
    Mientras No juego_terminado Hacer
        // --- 1. CARGAR MAPA (ARENA) ---
        mapa[1] = "########################################"
        mapa[2] = "#                                      #"
        mapa[3] = "#                                      #"
        mapa[4] = "#                                      #"
        mapa[5] = "#                                      #"
        mapa[6] = "#                                      #"
        mapa[7] = "#                  @                   #"
        mapa[8] = "#                                      #"
        mapa[9] = "#                                      #"
        mapa[10]= "#                                      #"
        mapa[11]= "#                                      #"
        mapa[12]= "#                                      #"
        mapa[13]= "#                                      #"
        mapa[14]= "#                                      #"
        mapa[15]= "########################################"
        filas = 15
        
        // --- 2. INICIALIZAR VARIABLES DE SUPERVIVENCIA ---
        Para k = 1 Hasta max_enemigos Hacer
            ene_activo[k] = Falso
        FinPara
        
        juego_perdido = Falso
        puntaje = 0
        turnos_jugados = 0
        prob_actual = 15 // Inicia con 15% de probabilidad de aparición
        
        // --- 3. ENCONTRAR AL JUGADOR ---
        Para i = 1 Hasta filas Hacer
            Para j = 1 Hasta Longitud(mapa[i]) Hacer
                char_p = Subcadena(mapa[i], j, j)
                Si char_p == "@" O char_p == "+" Entonces
                    py = i
                    px = j
                FinSi
            FinPara
        FinPara
        
        reiniciar_nivel = Falso
        
        // --- 4. BUCLE INFINITO DE SUPERVIVENCIA ---
        Mientras No reiniciar_nivel Y No juego_terminado Hacer
            Borrar Pantalla
            Escribir "=== ARENA DE SUPERVIVENCIA ==="
            Escribir "PUNTAJE: ", puntaje, " | Turnos sobrevividos: ", turnos_jugados
            Escribir "Probabilidad de aparicion enemiga: ", prob_actual, "%"
            Escribir "-----------------------------------------------------------------"
            Escribir "Mover: [W, A, S, D] | Disparo Recto: [I, J, K, L]"
            Escribir "Disparo Diag.: [U] Arr-Izq | [O] Arr-Der | [N] Aba-Izq | [M] Aba-Der"
            Escribir "[R] Reiniciar Partida | [Q] Salir del juego"
            Escribir "Simbolos: @ Tu | $ Caja | o Disparo | 1-4 Enemigos (Vida)"
            Escribir "-----------------------------------------------------------------"
            
            // Dibujar mapa
            Para i = 1 Hasta filas Hacer
                Escribir mapa[i]
            FinPara
            
            // Comprobar Derrota
            Si juego_perdido Entonces
                Escribir "-----------------------------------------------------------------"
                Escribir "¡TE HAN ATRAPADO! Fin del Juego."
                Escribir "Tu puntaje final es: ", puntaje
                Escribir "Sobreviviste ", turnos_jugados, " turnos."
                Escribir "Presiona [R] para jugar de nuevo o [Q] para salir."
                Leer tecla
                Si Minusculas(tecla) == "q" Entonces
                    juego_terminado = Verdadero
                Sino
                    reiniciar_nivel = Verdadero
                FinSi
            Sino
                // Leer comando del jugador
                Escribir ""
                Escribir "Tu accion + ENTER:"
                Leer tecla
                tecla = Minusculas(tecla)
                turno_valido = Falso
                
                Si tecla == "q" Entonces
                    juego_terminado = Verdadero
                Sino 
                    Si tecla == "r" Entonces
                        reiniciar_nivel = Verdadero
                    Sino
                        dx = 0
                        dy = 0
                        bx_dir = 0
                        by_dir = 0
                        
                        Segun tecla Hacer
                            "w": 
                                dy = -1
                                turno_valido = Verdadero
                            "s": 
                                dy = 1
                                turno_valido = Verdadero
                            "a": 
                                dx = -1
                                turno_valido = Verdadero
                            "d": 
                                dx = 1
                                turno_valido = Verdadero
                            "i": 
                                by_dir = -1
                                turno_valido = Verdadero
                            "k": 
                                by_dir = 1
                                turno_valido = Verdadero
                            "j": 
                                bx_dir = -1
                                turno_valido = Verdadero
                            "l": 
                                bx_dir = 1
                                turno_valido = Verdadero
                            "u": 
                                by_dir = -1
                                bx_dir = -1
                                turno_valido = Verdadero
                            "o": 
                                by_dir = -1
                                bx_dir = 1
                                turno_valido = Verdadero
                            "n": 
                                by_dir = 1
                                bx_dir = -1
                                turno_valido = Verdadero
                            "m": 
                                by_dir = 1
                                bx_dir = 1
                                turno_valido = Verdadero
                        FinSegun
                        
                        // LÓGICA DE MOVIMIENTO DEL JUGADOR
                        Si dx <> 0 O dy <> 0 Entonces
                            tx = px + dx
                            ty = py + dy
                            
                            Si ty >= 1 Y ty <= filas Y tx >= 1 Y tx <= Longitud(mapa[1]) Entonces
                                char_p = Subcadena(mapa[py], px, px)
                                char_t = Subcadena(mapa[ty], tx, tx)
                                
                                Si char_p == "@" Entonces 
                                    char_p_new = " " 
                                FinSi
                                Si char_p == "+" Entonces 
                                    char_p_new = "." 
                                FinSi
                                
                                // Lógica A: Moverse a vacío o punto
                                Si char_t == " " O char_t == "." Entonces
                                    Si char_t == " " Entonces 
                                        char_t_new = "@" 
                                    FinSi
                                    Si char_t == "." Entonces 
                                        char_t_new = "+" 
                                    FinSi
                                    mapa[py] = ModificarCadena(mapa[py], px, char_p_new)
                                    mapa[ty] = ModificarCadena(mapa[ty], tx, char_t_new)
                                    py = ty
                                    px = tx
                                    
                                Sino 
                                    // Lógica B: Chocar con un Enemigo
                                    Si char_t >= "1" Y char_t <= "4" Entonces
                                        mapa[py] = ModificarCadena(mapa[py], px, " ")
                                        juego_perdido = Verdadero
                                    FinSi
                                FinSi
                            FinSi
                        FinSi
                        
                        // LÓGICA DE DISPARO DEL JUGADOR (CON ANIMACIÓN)
                        Si bx_dir <> 0 O by_dir <> 0 Entonces
                            b_x = px + bx_dir
                            b_y = py + by_dir
                            bala_activa = Verdadero
                            
                            Mientras bala_activa Y b_y >= 1 Y b_y <= filas Y b_x >= 1 Y b_x <= Longitud(mapa[1]) Hacer
                                char_b = Subcadena(mapa[b_y], b_x, b_x)
                                
                                Si char_b == "#" O char_b == "$" O char_b == "*" Entonces
                                    bala_activa = Falso 
                                Sino
                                    Si char_b >= "1" Y char_b <= "4" Entonces
                                        Para k = 1 Hasta max_enemigos Hacer
                                            Si ene_activo[k] Y ene_x[k] == b_x Y ene_y[k] == b_y Entonces
                                                ene_hp[k] = ene_hp[k] - 1 
                                                puntaje = puntaje + 10 
                                                
                                                Si ene_hp[k] <= 0 Entonces
                                                    ene_activo[k] = Falso 
                                                    puntaje = puntaje + 50 
                                                    mapa[b_y] = ModificarCadena(mapa[b_y], b_x, " ")
                                                Sino
                                                    mapa[b_y] = ModificarCadena(mapa[b_y], b_x, ConvertirATexto(ene_hp[k]))
                                                FinSi
                                            FinSi
                                        FinPara
                                        bala_activa = Falso
                                    Sino
                                        // ANIMACIÓN DE LA BALA
                                        mapa[b_y] = ModificarCadena(mapa[b_y], b_x, "o") 
                                        
                                        Borrar Pantalla
                                        Escribir "=== ARENA DE SUPERVIVENCIA ==="
                                        Escribir "PUNTAJE: ", puntaje, " | Turnos sobrevividos: ", turnos_jugados
                                        Escribir "Probabilidad de aparicion enemiga: ", prob_actual, "%"
                                        Escribir "-----------------------------------------------------------------"
                                        Para i = 1 Hasta filas Hacer
                                            Escribir mapa[i]
                                        FinPara
                                        
                                        Esperar 50 Milisegundos
                                        
                                        mapa[b_y] = ModificarCadena(mapa[b_y], b_x, char_b)
                                        
                                        b_x = b_x + bx_dir
                                        b_y = b_y + by_dir
                                    FinSi
                                FinSi
                            FinMientras
                        FinSi
                        
                        // --- FASE DE LOS ENEMIGOS Y PROGRESIÓN ---
                        Si turno_valido Y No juego_perdido Entonces
                            turnos_jugados = turnos_jugados + 1
                            
                            // Aumentar la probabilidad linealmente cada 10 turnos (sin Fibonacci)
                            Si turnos_jugados MOD 10 == 0 Entonces
                                prob_actual = prob_actual + 5
                                Si prob_actual > 100 Entonces
                                    prob_actual = 100 
                                FinSi
                            FinSi
                            
                            // 1. SPAWNER BASADO EN PROBABILIDAD LINEAL
                            // Azar(100) genera de 0 a 99, le sumamos 1 para que sea de 1 a 100
                            Si (Azar(100) + 1) <= prob_actual Entonces
                                slot_libre = 0
                                k = 1
                                Mientras k <= max_enemigos Y slot_libre == 0 Hacer
                                    Si No ene_activo[k] Entonces
                                        slot_libre = k
                                    FinSi
                                    k = k + 1
                                FinMientras
                                
                                Si slot_libre > 0 Entonces
                                    spot_valido = Falso
                                    Mientras No spot_valido Hacer
                                        ry = Azar(filas) + 1
                                        rx = Azar(Longitud(mapa[1])) + 1
                                        Si ry >= 1 Y ry <= filas Y rx >= 1 Y rx <= Longitud(mapa[1]) Entonces
                                            Si Subcadena(mapa[ry], rx, rx) == " " Entonces
                                                Si abs(rx - px) + abs(ry - py) > 3 Entonces
                                                    spot_valido = Verdadero
                                                FinSi
                                            FinSi
                                        FinSi
                                    FinMientras
                                    
                                    ene_x[slot_libre] = rx
                                    ene_y[slot_libre] = ry
                                    
                                    // Vida aleatoria del 1 al 4 (Azar(4) genera de 0 a 3, + 1 da 1 a 4)
                                    ene_hp[slot_libre] = Azar(4) + 1 
                                    ene_activo[slot_libre] = Verdadero
                                    mapa[ry] = ModificarCadena(mapa[ry], rx, ConvertirATexto(ene_hp[slot_libre]))
                                FinSi
                            FinSi
                            
                            // 2. MOVIMIENTO DE ENEMIGOS VIVOS
                            Para k = 1 Hasta max_enemigos Hacer
                                Si ene_activo[k] Entonces
                                    dx_e = 0
                                    dy_e = 0
                                    
                                    Si px < ene_x[k] Entonces 
                                        dx_e = -1 
                                    Sino 
                                        Si px > ene_x[k] Entonces 
                                            dx_e = 1 
                                        FinSi 
                                    FinSi
                                    
                                    Si py < ene_y[k] Entonces 
                                        dy_e = -1 
                                    Sino 
                                        Si py > ene_y[k] Entonces 
                                            dy_e = 1 
                                        FinSi 
                                    FinSi
                                    
                                    movio = Falso
                                    
                                    Si dx_e <> 0 O dy_e <> 0 Entonces
                                        Si ene_y[k] + dy_e >= 1 Y ene_y[k] + dy_e <= filas Y ene_x[k] + dx_e >= 1 Y ene_x[k] + dx_e <= Longitud(mapa[1]) Entonces
                                            c_t = Subcadena(mapa[ene_y[k] + dy_e], ene_x[k] + dx_e, ene_x[k] + dx_e)
                                            Si c_t == " " O c_t == "@" O c_t == "+" Entonces
                                                nex = ene_x[k] + dx_e
                                                ney = ene_y[k] + dy_e
                                                movio = Verdadero
                                            Sino 
                                                Si dx_e <> 0 Entonces
                                                    Si ene_x[k] + dx_e >= 1 Y ene_x[k] + dx_e <= Longitud(mapa[1]) Entonces
                                                        c_t = Subcadena(mapa[ene_y[k]], ene_x[k] + dx_e, ene_x[k] + dx_e)
                                                        Si c_t == " " O c_t == "@" O c_t == "+" Entonces
                                                            nex = ene_x[k] + dx_e
                                                            ney = ene_y[k]
                                                            movio = Verdadero
                                                        FinSi
                                                    FinSi
                                                FinSi
                                                
                                                Si No movio Y dy_e <> 0 Entonces
                                                    Si ene_y[k] + dy_e >= 1 Y ene_y[k] + dy_e <= filas Entonces
                                                        c_t = Subcadena(mapa[ene_y[k] + dy_e], ene_x[k], ene_x[k])
                                                        Si c_t == " " O c_t == "@" O c_t == "+" Entonces
                                                            nex = ene_x[k]
                                                            ney = ene_y[k] + dy_e
                                                            movio = Verdadero
                                                        FinSi
                                                    FinSi
                                                FinSi
                                            FinSi
                                        FinSi
                                    FinSi
                                    
                                    Si movio Entonces
                                        mapa[ene_y[k]] = ModificarCadena(mapa[ene_y[k]], ene_x[k], " ")
                                        ene_x[k] = nex
                                        ene_y[k] = ney
                                        
                                        c_t = Subcadena(mapa[ney], nex, nex)
                                        Si c_t == "@" O c_t == "+" Entonces
                                            juego_perdido = Verdadero
                                            mapa[ney] = ModificarCadena(mapa[ney], nex, "X") 
                                        Sino
                                            mapa[ney] = ModificarCadena(mapa[ney], nex, ConvertirATexto(ene_hp[k]))
                                        FinSi
                                    FinSi
                                FinSi
                            FinPara
                        FinSi 
                    FinSi 
                FinSi 
            FinSi 
        FinMientras
    FinMientras
    
    Borrar Pantalla
    Escribir "Has salido de la Arena. ¡Hasta pronto!"
FinAlgoritmo