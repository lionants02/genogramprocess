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

internal class AlgorithmMapFather<P> {

    interface AddFatherBaseFunc<P> {
        val idCard: String
        val sex: GENOSEX?

        /**
         * พ่อที่ถูกกำหนดมาใน Entity
         */
        val fatherInRelation: P?

        fun setFather(fatherIdCard: String)
    }

    private fun P.addFather(
        father: Person<P>?,
        func: (person: P) -> AddFatherBaseFunc<P>
    ) {
        if (father != null && func(father.person).sex != GENOSEX.FEMALE) {
            if (func(father.person).idCard == func(this).idCard) return
            func(this).setFather(func(father.person).idCard)
        }
    }

    interface MapFatherByIdGetData<P> : AddFatherBaseFunc<P> {
        override val idCard: String
        val fatherInformationIdCard: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapFatherById(persons: List<Person<P>>, func: (person: P) -> MapFatherByIdGetData<P>) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have father
            if (func(focusPerson).fatherInRelation == null) {
                val fatherInformationIdCard = func(focusPerson).fatherInformationIdCard
                if (!fatherInformationIdCard.isNullOrBlank()) {
                    val father = persons.find { func(it.person).idCard == fatherInformationIdCard }
                    focusPerson.addFather(father, func)
                }
            }
        }
    }

    interface MapFatherByName<P> : AddFatherBaseFunc<P> {
        val name: String
        val age: Int
        val fatherName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     * @param personGroupHouse จัดกลุ่มคนในบ้านเพื่อลดการค้นหม pcucode, houseNumber, รายการคน
     */
    fun mapFatherByName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapFatherByName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have father
            if (func(focusPerson).fatherInRelation != null) return@forEach

            val fatherName = func(focusPerson).fatherName
            if (!fatherName.isNullOrBlank()) {
                val father = personGroupHouse[(person.pcucode to person.houseNumber)]
                    ?.find { func(it.person).name == fatherName }
                father?.let {
                    if (func(it.person).age > 15) focusPerson.addFather(father, func)
                }
            }
        }
    }

    interface MapFatherByFirstName<P> : AddFatherBaseFunc<P> {
        val firstName: String
        val lastName: String
        val age: Int
        val fatherFirstName: String?
    }

    /**
     * @param persons คนที่แมพ pcucode, houseNumber, รายการคน
     */
    fun mapFatherByFirstName(
        persons: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>,
        func: (person: P) -> MapFatherByFirstName<P>
    ) {
        persons.forEach { person ->
            val focusPerson = person.person
            // check have father
            if (func(focusPerson).fatherInRelation != null) return@forEach

            val fatherFirstName = func(focusPerson).fatherFirstName
            if (!fatherFirstName.isNullOrBlank()) {
                val father = personGroupHouse[(person.pcucode to person.houseNumber)]
                    ?.find { func(it.person).firstName == fatherFirstName }
                father?.let {
                    if (func(it.person).lastName == func(focusPerson).lastName && func(it.person).age > 15)
                        focusPerson.addFather(father, func)
                }
            }
        }
    }
}
