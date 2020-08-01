/*
 * Copyright (c) 2020 NSTDA
 *   National Science and Technology Development Agency, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package genogram

import genogram.GENOSEX.FEMALE
import genogram.GENOSEX.MALE
import kotlin.math.absoluteValue

internal class AlgorithmMapMate<P> {

    interface AddMateBaseFunc<P> {
        val idCard: String
        val sex: GENOSEX?

        /**
         * แฟนที่ถูกกำหนดมาใน Entity
         */
        val mateInRelation: List<P>

        fun addMate(mateIdCard: String)
    }

    private fun P.addMate(
        mate: Person<P>?,
        func: (person: P) -> AddMateBaseFunc<P>
    ) {
        if (mate != null)
            if (isMate(func(this).sex, func(mate.person).sex)) {
                if (func(this).mateInRelation.find { func(it).idCard == func(mate.person).idCard } != null) return
                func(this).addMate(func(mate.person).idCard)
            }
    }

    /**
     * ตรวจสอบเพศ
     */
    private fun isMate(focusPersonSex: GENOSEX?, mateSex: GENOSEX?): Boolean {
        return if (focusPersonSex == FEMALE) {
            when (mateSex) {
                MALE -> true
                null -> true
                else -> false
            }
        } else if (focusPersonSex == MALE) {
            when (mateSex) {
                FEMALE -> true
                null -> true
                else -> false
            }
        } else
            return true
    }

    interface MapMateByIdGetData<P> : AddMateBaseFunc<P> {
        override val idCard: String
        val mateInformationIdCard: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMateById(persons: List<Person<P>>, func: (person: P) -> MapMateByIdGetData<P>) {
        persons.forEach { person ->
            val focusPerson = person.person

            val mateInformationIdCard = func(focusPerson).mateInformationIdCard
            if (!mateInformationIdCard.isNullOrBlank()) {
                val mate = persons.find { func(it.person).idCard == mateInformationIdCard }
                focusPerson.addMate(mate, func)
            }
        }
    }

    interface MapMateByName<P> : AddMateBaseFunc<P> {
        val name: String
        val age: Int
        val mateName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMateByName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapMateByName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person

            val mateName = func(focusPerson).mateName
            if (!mateName.isNullOrBlank()) {
                val mate = personGroupHouse[(person.pcucode to person.houseNumber)]
                    ?.find { func(it.person).name == mateName }
                mate?.let {
                    if (func(it.person).age.checkAgeMate(func(focusPerson).age)) focusPerson.addMate(mate, func)
                }
            }
        }
    }

    private fun Int.checkAgeMate(mateAge: Int): Boolean {
        val cal = (this - mateAge).absoluteValue
        return cal <= 5
    }

    interface MapMateByFirstName<P> : AddMateBaseFunc<P> {
        val firstName: String
        val age: Int
        val mateFirstName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMateByFirstName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapMateByFirstName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person
            val mateFirstName = func(focusPerson).mateFirstName
            if (!mateFirstName.isNullOrBlank()) {
                val mate = personGroupHouse[(person.pcucode to person.houseNumber)]
                    ?.find { func(it.person).firstName == mateFirstName }
                mate?.let {
                    if (func(it.person).age.checkAgeMate(func(focusPerson).age))
                        focusPerson.addMate(mate, func)
                }
            }
        }
    }
}
