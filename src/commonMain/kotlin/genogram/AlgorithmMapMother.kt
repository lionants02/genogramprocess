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

internal class AlgorithmMapMother<P> {

    interface AddMotherBaseFunc<P> {
        val idCard: String
        val sex: GENOSEX?

        /**
         * พ่อที่ถูกกำหนดมาใน Entity
         */
        val motherInRelation: P?

        fun setMother(motherIdCard: String)
    }

    private fun P.addMother(
        mother: Person<P>?,
        func: (person: P) -> AddMotherBaseFunc<P>
    ) {
        if (mother != null && func(mother.person).sex != GENOSEX.MALE) {
            if (func(mother.person).idCard == func(this).idCard) return
            func(this).setMother(func(mother.person).idCard)
        }
    }

    interface MapMotherByIdGetData<P> : AddMotherBaseFunc<P> {
        override val idCard: String
        val motherInformationIdCard: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMotherById(persons: List<Person<P>>, func: (person: P) -> MapMotherByIdGetData<P>) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have mother
            if (func(focusPerson).motherInRelation != null) return@forEach

            val motherInformationIdCard = func(focusPerson).motherInformationIdCard
            if (!motherInformationIdCard.isNullOrBlank()) {
                val mother = persons.find { func(it.person).idCard == motherInformationIdCard }
                focusPerson.addMother(mother, func)
            }
        }
    }

    interface MapMotherByName<P> : AddMotherBaseFunc<P> {
        val name: String
        val age: Int
        val motherName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMotherByName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapMotherByName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have mother
            if (func(focusPerson).motherInRelation == null) {
                val motherName = func(focusPerson).motherName
                if (!motherName.isNullOrBlank()) {
                    val mother = personGroupHouse[(person.pcucode to person.houseNumber)]
                        ?.find { func(it.person).name == motherName }
                    mother?.let {
                        if (func(it.person).age > 15) focusPerson.addMother(mother, func)
                    }
                }
            }
        }
    }

    interface MapMotherByFirstName<P> : AddMotherBaseFunc<P> {
        val firstName: String
        val lastName: String
        val age: Int
        val motherFirstName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapMotherByFirstName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapMotherByFirstName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have mother
            if (func(focusPerson).motherInRelation != null) return@forEach

            val motherFirstName = func(focusPerson).motherFirstName
            if (!motherFirstName.isNullOrBlank()) {
                val mother = personGroupHouse[(person.pcucode to person.houseNumber)]
                    ?.find { func(it.person).firstName == motherFirstName }
                mother?.let {
                    if (func(it.person).lastName == func(focusPerson).lastName && func(it.person).age > 15)
                        focusPerson.addMother(mother, func)
                }
            }
        }
    }
}
