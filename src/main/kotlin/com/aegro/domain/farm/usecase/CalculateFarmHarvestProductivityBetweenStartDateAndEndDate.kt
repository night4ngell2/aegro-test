package com.aegro.domain.farm.usecase

import com.aegro.domain.exception.NotFoundException
import com.aegro.domain.farm.gateway.outbound.FindFarmByIdOutbound
import com.aegro.domain.farm.model.Plot
import com.aegro.domain.result.model.Failure
import com.aegro.domain.result.model.Result
import com.aegro.domain.result.model.Success
import com.aegro.domain.result.model.onFailure
import java.time.LocalDate
import javax.inject.Named

@Named
class CalculateFarmHarvestProductivityBetweenStartDateAndEndDate(
    private val findFarmByIdOutbound: FindFarmByIdOutbound
): CalculateFarmProductivityDateFilterStrategy {

    override suspend fun validateFilter(startDate: LocalDate?, endDate: LocalDate?) =
        startDate != null && endDate != null


    override suspend fun execute(farmId: String, startDate: LocalDate?, endDate: LocalDate?): Result<Int, Exception> {
        val (_, _, plots) = findFarmByIdOutbound.execute(farmId).onFailure { return it }
        return calculateFarmHarvestsProductivityBetweenStartDateAndEndDate(plots, startDate!!, endDate!!)
            ?.let {
                Success(it)
            } ?: Failure(NotFoundException(code = "farm.plot.harvest.not-found-productivity"))
    }

    private fun calculateFarmHarvestsProductivityBetweenStartDateAndEndDate(plots: List<Plot>?, startDate: LocalDate, endDate: LocalDate): Int? {
        val a = plots?.mapNotNull {
            it.harvests
        }?.flatten()
            ?.filter { harvest ->
                harvest.dateAndTime.toLocalDate() >= startDate && harvest.dateAndTime.toLocalDate() <= endDate
            }
            ?.map {
                it.productivity
            }?.reduceOrNull { acc, i -> acc + i }
        return a
    }


}