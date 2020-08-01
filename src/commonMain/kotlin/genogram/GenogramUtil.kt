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

internal class GenogramUtil<P> {

    /**
     * สำหรับขั้นตอนการเตรียมข้อมูล
     * ทำให้แยก unit test ได้สะดวกขึ้น
     */
    interface PreFunctionGetData {
        val pcuCode: String?
        val houseNumber: String?
        val name: String?
    }

    /**
     * ใช้สำหรับเตรียมข้อมูลที่จำเป็นก่อนการประมวลผล
     * สาเหตุเพราะ ข้อมูลชุดเต็ม มีโครงสร้างที่ซับซ้อนที่ไม่จำเป็น
     * เกินไปสำหรับการสร้าง Genogram ในที่นี้หลักๆ
     * จะใช้เลขบ้าน กับรหัส pcucode เพื่อใช้ในการจัดกลุ่มต่าง ๆ
     * @return ข้อมูลที่ผ่านการจัดเตรียมแล้ว
     */
    fun prepareInformation(
        persons: List<P>,
        preFunctionGetData: (person: P) -> PreFunctionGetData
    ): List<Person<P>> {
        return persons.mapNotNull {
            // it.link?.keys?.get("pcucodeperson")?.toString()
            val pcuCode = preFunctionGetData(it).pcuCode
            // it.link?.keys?.get("hcode")?.toString()
            val houseNumber = preFunctionGetData(it).houseNumber
            val name = preFunctionGetData(it).name
            when {
                pcuCode == null -> null
                houseNumber == null -> null
                else -> {
                    Person(pcuCode, houseNumber, it, name)
                }
            }
        }
    }

    /**
     * ทำการค้นหา คนที่อยู่ในบ้าน
     * @return แมพ pcucode, houseNumber, รายการคน
     */
    fun personGroupHouse(persons: List<Person<P>>): Map<Pair<String, String>, List<Person<P>>> {
        val setData = HashSet<Pair<String, String>>()
        persons.forEach { setData.add(it.pcucode to it.houseNumber) }

        return setData.map { key ->
            key to persons.filter { it.pcucode == key.first && it.houseNumber == key.second }
        }.toMap()
    }
}
