/*
 *
 * este codigo cuenta con la participacion de Rubén Hernan Barrera Chavez
 *
 */

package com.gruposolux.rcivil.pdisciplinario.service;
import  org.springframework.stereotype.Service;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("fechasService")
public class FechasService {

    public Date  calcularFecha(Date fecha, int dias,List<Date> listaFechasNoLaborables){
//        Calendar calendar = Calendar.getInstance();
        Calendar fechaInicial = Calendar.getInstance();
        Calendar fechaFinal = Calendar.getInstance();
//        calendar.setTime(fecha);
//        calendar.add(Calendar.DAY_OF_YEAR,dias);

        fechaInicial.setTime(fecha);


        boolean diaHabil = false;
        int diffDays = 0;
//
//        while (fechaInicial.before(fechaFinal) || fechaInicial.equals(fechaFinal)) {
        while  (dias != diffDays) {
            System.out.println("dias a sumar: "+dias);
            System.out.println("contador de dias: "+diffDays);
            System.out.println("entro al while ");
            fechaInicial.add(Calendar.DAY_OF_YEAR,1);
            if (!listaFechasNoLaborables.isEmpty()) {
                for (Date date : listaFechasNoLaborables) {
//                if (!listaFechasNoLaborables.isEmpty()) {
                    Date fechaNoLaborablecalendar = fechaInicial.getTime();
                    //si el dia de la semana de la fecha minima es diferente de sabado o domingo
                    if (fechaInicial.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && fechaInicial.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && !fechaNoLaborablecalendar.equals(date)) {
                        //se aumentan los dias de diferencia entre min y max
                        diaHabil = true;

                    } else {
                        diaHabil = false;
                        break;
                    }
                }
//                }
            } else {

                if (fechaInicial.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && fechaInicial.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                    //se aumentan los dias de diferencia entre min y max
                    diffDays=diffDays+1;
                    System.out.println("contador: "+diffDays);

                    System.out.println("sumando dias: "+fechaInicial.getTime());
                }
            }
            if (diaHabil == true) {

                diffDays=diffDays+1;

            }

        }

        fechaFinal.setTime(fechaInicial.getTime());

        System.out.println("terminado el cliclo fecha final:"+fechaFinal.getTime());
        return  fechaFinal.getTime();
    }


}
