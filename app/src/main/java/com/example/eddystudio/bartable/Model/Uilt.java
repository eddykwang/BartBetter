package com.example.eddystudio.bartable.Model;

import android.graphics.Color;

public class Uilt {

  public static String getFullStationName(String shortName){
    String fullName;
    switch (shortName){
      case "12TH": fullName = "12th St. Oakland City Center"; break;
      case "16TH": fullName = "16th St. Mission"; break;
      case "19TH": fullName = "19th St. Oakland"; break;
      case "24TH": fullName = "24th St. Mission"; break;
      case "ASHB": fullName = "Ashby"; break;
      case "BALB": fullName = "Balboa Park"; break;
      case "BAYF": fullName = "Bay Fair"; break;
      case "CAST": fullName = "Castro Valley"; break;
      case "CIVC": fullName = "Civic Center/UN Plaza"; break;
      case "COLS": fullName = "Coliseum"; break;
      case "COLM": fullName = "Colma"; break;
      case "CONC": fullName = "Concord"; break;
      case "DALY": fullName = "Daly City"; break;
      case "DBRK": fullName = "Downtown Berkeley"; break;
      case "DUBL": fullName = "Dublin/Pleasanton"; break;
      case "DELN": fullName = "El Cerrito del Norte"; break;
      case "PLZA": fullName = "El Cerrito Plaza"; break;
      case "EMBR": fullName = "Embarcadero"; break;
      case "FRMT": fullName = "Fremont"; break;
      case "FTVL": fullName = "Fruitvale"; break;
      case "GLEN": fullName = "Glen Park"; break;
      case "HAYW": fullName = "Hayward"; break;
      case "LAFY": fullName = "Lafayette"; break;
      case "LAKE": fullName = "Lake Merritt"; break;
      case "MCAR": fullName = "MacArthur"; break;
      case "MLBR": fullName = "Millbrae"; break;
      case "MONT": fullName = "Montgomery St."; break;
      case "NBRK": fullName = "North Berkeley"; break;
      case "NCON": fullName = "North Concord/Martinez"; break;
      case "OAKL": fullName = "Oakland International Airport"; break;
      case "ORIN": fullName = "Orinda"; break;
      case "PITT": fullName = "Pittsburg/Bay Point"; break;
      case "PHIL": fullName = "Pleasant Hill/Contra Costa Centre"; break;
      case "POWL": fullName = "Powell St."; break;
      case "RICH": fullName = "Richmond"; break;
      case "ROCK": fullName = "Rockridge"; break;
      case "SBRN": fullName = "San Bruno"; break;
      case "SFIA": fullName = "San Francisco International Airport"; break;
      case "SANL": fullName = "San Leandro"; break;
      case "SHAY": fullName = "South Hayward"; break;
      case "SSAN": fullName = "South San Francisco"; break;
      case "UCTY": fullName = "Union City"; break;
      case "WCRK": fullName = "Walnut Creek"; break;
      case "WARM": fullName = "Warm Springs/South Fremont"; break;
      case "WDUB": fullName = "West Dublin/Pleasanton"; break;
      case "WOAK": fullName = "West Oakland"; break;
      default: fullName = shortName;
    }
    return fullName;
  }

  public static int materialColorConverter(String color){
    int mColor = 1;
    switch (color){
      case "GREEN": mColor = Color.parseColor("#43A047"); break;
      case "BLUE": mColor = Color.parseColor("#1E88E5") ;break;
      case "RED": mColor = Color.parseColor("#E53935") ; break;
      case "YELLOW": mColor = Color.parseColor("#FDD835") ; break;
      case "ORANGE": mColor = Color.parseColor("#FB8C00") ; break;
      default: mColor = Color.parseColor("#5D4037") ;
    }
    return mColor;
  }

}
