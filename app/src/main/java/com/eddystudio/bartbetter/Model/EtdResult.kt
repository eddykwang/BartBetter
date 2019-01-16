package com.eddystudio.bartbetter.Model

import com.eddystudio.bartbetter.Model.Response.EstimateResponse.Etd

data class EtdResult(val etd: List<Etd>, val origin: String, val destination: String) {

}