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

/**
 * อังกอริทึมหาความสัมพันธิ์
 * @param dataFunction function สำหรับการเรียกใช้ข้อมูลต่างๆจากต้นทาง
 */
class GenogramProcessWatcarakorn<P>(
    private val dataFunction: PersonDetailInterface<P>
) : GenogramProcess<P> {
    private val logger = getLogger(this::class.simpleName!!)
    private val util = GenogramUtil<P>()

    /**
     * ประมวลผล Genogram
     * @param persons รายการคนทั้งหมด
     */
    override fun process(persons: List<P>) {
        logger.info { "เตรียมข้อมูลวิเคราะห์ความสัมพันธุ์" }
        val preData = util.prepareInformation(persons) {
            object : GenogramUtil.PreFunctionGetData {
                override val pcuCode: String? = dataFunction.getPcuCode(it)
                override val houseNumber: String? = dataFunction.getHouseNumber(it)
                override val name: String? = "${dataFunction.getFirstName(it)} ${dataFunction.getLastName(it)}"
            }
        }

        logger.info { "จัดกลุ่มคนในบ้าน" }
        val personGroupHouse = util.personGroupHouse(preData)
        logger.info { "จัดความสัมพันธุ์พ่อ" }
        fatherProcess(preData, personGroupHouse)
        logger.info { "จัดความสัมพันธุ์แม่" }
        motherProcess(preData, personGroupHouse)
        logger.info { "จัดความสัมพันธุ์แฟน" }
        mateProcess(preData, personGroupHouse)
    }

    private fun mateProcess(
        preData: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>
    ) {
        val algorithmMapMate = AlgorithmMapMate<P>()
        logger.info { "ค้นหาแฟนด้วยเลขบัตรประชาชน" }
        algorithmMapMate.mapMateById(preData) {
            object : AlgorithmMapMate.MapMateByIdGetData<P> {
                override val mateInformationIdCard: String? = dataFunction.getMateInformationId(it)
                override val mateInRelation: List<P> = dataFunction.getMateInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun addMate(mateIdCard: String) {
                    dataFunction.addMate(it, mateIdCard)
                }
            }
        }
        logger.info { "ค้นหาแฟนด้วยชื่อนามสกุล" }
        algorithmMapMate.mapMateByName(preData, personGroupHouse) {
            object : AlgorithmMapMate.MapMateByName<P> {
                override val name: String = "${dataFunction.getFirstName(it)} ${dataFunction.getLastName(it)}"
                override val mateName: String?
                    get() = {
                        val firstName = dataFunction.getMateFirstName(it)
                        val lastName = dataFunction.getMateLastName(it)

                        if (firstName.isNullOrBlank() || lastName.isNullOrBlank())
                            null
                        else
                            "$firstName $lastName"
                    }.invoke()
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val mateInRelation: List<P> = dataFunction.getMateInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun addMate(mateIdCard: String) {
                    dataFunction.addMate(it, mateIdCard)
                }
            }
        }
        logger.info { "ค้นหาแฟนด้วยชื่อ" }
        algorithmMapMate.mapMateByFirstName(preData, personGroupHouse) {
            object : AlgorithmMapMate.MapMateByFirstName<P> {
                override val firstName: String = dataFunction.getFirstName(it)
                override val mateFirstName: String? = dataFunction.getMateFirstName(it)
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val mateInRelation: List<P> = dataFunction.getMateInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun addMate(mateIdCard: String) {
                    dataFunction.addMate(it, mateIdCard)
                }
            }
        }
    }

    private fun motherProcess(
        preData: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>
    ) {
        val algorithmMapMother = AlgorithmMapMother<P>()
        logger.info { "ค้นหาแม่ด้วยเลขบัตรประชาชน" }
        algorithmMapMother.mapMotherById(preData) {
            object : AlgorithmMapMother.MapMotherByIdGetData<P> {
                override val motherInformationIdCard: String? = dataFunction.getMotherInformationId(it)
                override val motherInRelation: P? = dataFunction.getMotherInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun setMother(motherIdCard: String) {
                    dataFunction.setMother(it, motherIdCard)
                }
            }
        }
        logger.info { "ค้นหาแม่ด้วยชื่อสกุล" }
        algorithmMapMother.mapMotherByName(preData, personGroupHouse) {
            object : AlgorithmMapMother.MapMotherByName<P> {
                override val name: String = "${dataFunction.getFirstName(it)} ${dataFunction.getLastName(it)}"
                override val motherName: String?
                    get() = {
                        val firstName = dataFunction.getMotherFirstName(it)
                        val lastName = dataFunction.getMotherLastName(it)

                        if (firstName.isNullOrBlank() || lastName.isNullOrBlank())
                            null
                        else
                            "$firstName $lastName"
                    }.invoke()
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val motherInRelation: P? = dataFunction.getMotherInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun setMother(motherIdCard: String) {
                    dataFunction.setMother(it, motherIdCard)
                }
            }
        }
        logger.info { "ค้นหาแม่ด้วยชื่อ" }
        algorithmMapMother.mapMotherByFirstName(preData, personGroupHouse) {
            object : AlgorithmMapMother.MapMotherByFirstName<P> {
                override val firstName: String = dataFunction.getFirstName(it)
                override val lastName: String = dataFunction.getLastName(it)
                override val motherFirstName: String? = dataFunction.getMotherFirstName(it)
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val motherInRelation: P? = dataFunction.getMotherInRelation(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override fun setMother(motherIdCard: String) {
                    dataFunction.setMother(it, motherIdCard)
                }
            }
        }
    }

    private fun fatherProcess(
        preData: List<Person<P>>,
        personGroupHouse: Map<Pair<String, String>, List<Person<P>>>
    ) {
        val algorithmMapFather = AlgorithmMapFather<P>()
        logger.info { "ค้นหาพ่อด้วยเลขบัตรประชาชน" }
        algorithmMapFather.mapFatherById(preData) {
            object : AlgorithmMapFather.MapFatherByIdGetData<P> {
                override val fatherInformationIdCard: String? = dataFunction.getFatherInformationId(it)
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override val fatherInRelation: P? = dataFunction.getFatherInRelation(it)
                override fun setFather(fatherIdCard: String) {
                    dataFunction.setFather(it, fatherIdCard)
                }
            }
        }
        logger.info { "ค้นหาพ่อด้วยชื่อนามสกุล" }
        algorithmMapFather.mapFatherByName(preData, personGroupHouse) {
            object : AlgorithmMapFather.MapFatherByName<P> {
                override val name: String = "${dataFunction.getFirstName(it)} ${dataFunction.getLastName(it)}"
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val fatherName: String?
                    get() = {
                        val firstName = dataFunction.getFatherFirstName(it)
                        val lastName = dataFunction.getFatherLastName(it)

                        if (firstName.isNullOrBlank() || lastName.isNullOrBlank())
                            null
                        else
                            "$firstName $lastName"
                    }.invoke()

                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override val fatherInRelation: P? = dataFunction.getFatherInRelation(it)
                override fun setFather(fatherIdCard: String) {
                    dataFunction.setFather(it, fatherIdCard)
                }
            }
        }
        logger.info { "ค้นหาพ่อด้วยชื่ออย่างเดียว" }
        algorithmMapFather.mapFatherByFirstName(preData, personGroupHouse) {
            object : AlgorithmMapFather.MapFatherByFirstName<P> {
                override val firstName: String = dataFunction.getFirstName(it)
                override val lastName: String = dataFunction.getLastName(it)
                override val fatherFirstName: String? = dataFunction.getFatherFirstName(it)
                override val age: Int = dataFunction.getAge(it) ?: 0
                override val idCard: String = dataFunction.getIdCard(it)
                override val sex: GENOSEX? = dataFunction.getSex(it)
                override val fatherInRelation: P? = dataFunction.getFatherInRelation(it)
                override fun setFather(fatherIdCard: String) {
                    dataFunction.setFather(it, fatherIdCard)
                }
            }
        }
    }
}
