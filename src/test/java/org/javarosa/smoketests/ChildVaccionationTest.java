/*
 * Copyright 2020 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.smoketests;


import static org.javarosa.core.test.Scenario.getRef;
import static org.javarosa.smoketests.ChildVaccionationTest.Sex.FEMALE;
import static org.javarosa.smoketests.ChildVaccionationTest.Sex.MALE;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.DIPHTERIA;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.DIPHTERIA_AND_MEASLES;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.DIPHTERIA_FIRST;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.DIPHTERIA_FIRST_AND_MEASLES;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.MEASLES;
import static org.javarosa.smoketests.ChildVaccionationTest.Vaccines.NONE;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.Scenario;
import org.junit.Test;

public class ChildVaccionationTest {

    public static final TreeReference NEXT_CHILD_REF = getRef("/data/household/child_repeat/nextChild");
    public static final TreeReference NEXT_CHILD_NO_MOTHER_REF = getRef("/data/household/child_repeat/nextChild_no_mother");
    public static final TreeReference FINAL_FLAT_REF = getRef("/data/household/finalflat");
    public static final TreeReference VACCINATION_PENTA1_REF = getRef("/data/household/child_repeat/penta1");
    public static final TreeReference VACCINATION_PENTA3_REF = getRef("/data/household/child_repeat/penta3");
    public static final TreeReference VACCINATION_MEASLES_REF = getRef("/data/household/child_repeat/mcv1");
    public static final TreeReference NOT_ELIG_NOTE_REF = getRef("/data/household/child_repeat/not_elig_note");
    public static final LocalDate TODAY = LocalDate.now();
    public static final TreeReference CHILD_REPEAT_REF = getRef("/data/household/child_repeat");

    @Test
    public void smoke_test() {
        Scenario scenario = Scenario.init("child_vaccination_VOL_tool_v12.xml");

        scenario.next();
        scenario.answer("multi");
        scenario.next();
        scenario.next();
        scenario.answer("1.234 5.678");
        scenario.next();
        scenario.answer("Some building");
        scenario.next();
        scenario.answer("Some address, some location");

        // Start filling in households

        AtomicInteger householdSeq = new AtomicInteger(1);
        List<List<Consumer<Integer>>> households = HealthRecord.all().stream().flatMap(healthRecord -> buildHouseholds(scenario, healthRecord).stream()).collect(Collectors.toList());
        for (int i = 0; i < households.size(); i++) {
            answerHousehold(scenario, householdSeq.getAndIncrement(), households.get(i));
            if (i + 1 < households.size()) {
                scenario.next();
                scenario.answer("no");
                scenario.next();
            }
        }

        scenario.trace("END HOUSEHOLDS");
        scenario.next();
        scenario.answer("yes");

        // Go to the end of the form
        scenario.next();
        scenario.next();
    }

    private List<List<Consumer<Integer>>> buildHouseholds(Scenario scenario, HealthRecord healthRecord) {
        return Arrays.asList(
            buildChildrenWithLocalDates(scenario, 23, healthRecord),
            buildChildrenWithIntegers(scenario, 23, healthRecord),
            buildChildrenWithLocalDates(scenario, 6, healthRecord),
            buildChildrenWithIntegers(scenario, 6, healthRecord),
            buildChildrenWithLocalDates(scenario, 3, healthRecord),
            buildChildrenWithIntegers(scenario, 3, healthRecord)
        );
    }

    private List<Consumer<Integer>> buildChildrenWithIntegers(Scenario scenario, int ageInMonths, HealthRecord healthRecord) {
        return Arrays.asList(
            answerChild(scenario, healthRecord, ageInMonths, NONE, MALE),
            answerChild(scenario, healthRecord, ageInMonths, DIPHTERIA_FIRST, FEMALE),
            answerChild(scenario, healthRecord, ageInMonths, DIPHTERIA, MALE),
            answerChild(scenario, healthRecord, ageInMonths, MEASLES, FEMALE),
            answerChild(scenario, healthRecord, ageInMonths, DIPHTERIA_FIRST_AND_MEASLES, MALE),
            answerChild(scenario, healthRecord, ageInMonths, DIPHTERIA_AND_MEASLES, FEMALE)
        );
    }

    private List<Consumer<Integer>> buildChildrenWithLocalDates(Scenario scenario, int ageInMonths, HealthRecord healthRecord) {
        LocalDate dob = TODAY.minusMonths(ageInMonths);
        return Arrays.asList(
            answerChild(scenario, healthRecord, dob, NONE, FEMALE),
            answerChild(scenario, healthRecord, dob, DIPHTERIA_FIRST, MALE),
            answerChild(scenario, healthRecord, dob, DIPHTERIA, FEMALE),
            answerChild(scenario, healthRecord, dob, MEASLES, MALE),
            answerChild(scenario, healthRecord, dob, DIPHTERIA_FIRST_AND_MEASLES, FEMALE),
            answerChild(scenario, healthRecord, dob, DIPHTERIA_AND_MEASLES, MALE)
        );
    }

    private void answerHousehold(Scenario scenario, int number, List<Consumer<Integer>> children) {
        scenario.trace("HOUSEHOLD " + number);
        scenario.next();
        scenario.next();
        scenario.answer(number);
        // Does someone answer the door?
        scenario.next();
        scenario.answer("yes");
        // Is there an adult
        scenario.next();
        scenario.answer("yes");
        // Do children under 2 live in the house?
        scenario.next();
        scenario.answer("yes");
        // What's the mother's or caregiver's name
        scenario.next();
        scenario.answer("Foo");
        // Is the mother or caregiver present?
        scenario.next();
        scenario.answer("yes");
        // Give consent
        scenario.next();
        scenario.answer("yes");
        // How many children under 2?
        scenario.next();
        scenario.answer(children.size());

        answerChildren(scenario, children);
    }


    enum Vaccines {
        NONE(false, false, false),
        DIPHTERIA_FIRST(true, false, false),
        DIPHTERIA(true, true, false),
        MEASLES(false, false, true),
        DIPHTERIA_FIRST_AND_MEASLES(true, false, true),
        DIPHTERIA_AND_MEASLES(true, true, true);

        static List<TreeReference> END_OF_VISIT_REFS = Arrays.asList(NEXT_CHILD_REF, FINAL_FLAT_REF, CHILD_REPEAT_REF);

        private final boolean diphteriaFirst;
        private final boolean diphteriaThird;
        private final boolean measles;

        Vaccines(boolean diphteriaFirst, boolean diphteriaThird, boolean measles) {
            this.diphteriaFirst = diphteriaFirst;
            this.diphteriaThird = diphteriaThird;
            this.measles = measles;
        }

        void visit(Scenario scenario) {
            // Answer questions until there's no more vaccination related questions
            while (!(END_OF_VISIT_REFS.contains(scenario.nextRef().genericize()))) {
                scenario.next();
                if (scenario.refAtIndex().genericize().equals(VACCINATION_PENTA1_REF))
                    scenario.answer(diphteriaFirst ? "yes" : "no");
                else if (scenario.refAtIndex().genericize().equals(VACCINATION_PENTA3_REF))
                    scenario.answer(diphteriaThird ? "yes" : "no");
                else if (scenario.refAtIndex().genericize().equals(VACCINATION_MEASLES_REF))
                    scenario.answer(measles ? "yes" : "no");
            }
        }
    }

    enum HealthRecord {
        HEALTH_HANDBOOK,
        VACCINATION_CARD,
        HEALTH_CLINIC;

        public static List<HealthRecord> all() {
            return Arrays.asList(values());
        }

        void visit(Scenario scenario) {
            if (this == HEALTH_HANDBOOK) {
                scenario.next();
                scenario.answer("yes");
            } else if (this == VACCINATION_CARD) {
                scenario.next();
                scenario.answer("no");
                scenario.next();
                scenario.answer("yes");
            } else if (this == HEALTH_CLINIC) {
                scenario.next();
                scenario.answer("no");
                scenario.next();
                scenario.answer("no");
                scenario.next();
                scenario.answer("yes");
            }
        }
    }

    enum Sex {
        FEMALE("female"), MALE("male");

        private final String name;

        Sex(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private void answerChildren(Scenario scenario, List<Consumer<Integer>> children) {
        for (int i = 0; i < children.size(); i++)
            children.get(i).accept(i + 1);
        scenario.trace("END CHILDREN");
    }

    private Consumer<Integer> answerChild(Scenario scenario, HealthRecord healthRecord, LocalDate dob, Vaccines vaccines, Sex sex) {
        return i -> {
            int ageInMonths = dob.until(TODAY).getMonths();
            String name = String.format("CHILD %d - Age %d months - %s", i, ageInMonths, sex.getName());
            scenario.trace(name);
            scenario.next();
            scenario.next();
            scenario.answer(name);
            healthRecord.visit(scenario);
            scenario.next();
            scenario.answer(sex.getName());
            answerDateOfBirth(scenario, dob);
            if (scenario.nextRef().genericize().equals(NOT_ELIG_NOTE_REF))
                scenario.next();
            else if (scenario.nextRef().genericize().equals(VACCINATION_PENTA1_REF))
                vaccines.visit(scenario);

            if (Arrays.asList(NEXT_CHILD_REF, NEXT_CHILD_NO_MOTHER_REF).contains(scenario.nextRef().genericize()))
                scenario.next();
        };
    }

    private Consumer<Integer> answerChild(Scenario scenario, HealthRecord healthRecord, int ageInMonths, Vaccines vaccines, Sex sex) {
        return i -> {
            String name = String.format("CHILD %d - Age %d months - %s", i, ageInMonths, sex.getName());
            scenario.trace(name);
            scenario.next();
            scenario.next();
            scenario.answer(name);
            healthRecord.visit(scenario);
            scenario.next();
            scenario.answer(sex.getName());
            answerAgeInMonths(scenario, ageInMonths);
            if (scenario.nextRef().genericize().equals(NOT_ELIG_NOTE_REF))
                scenario.next();
            else if (scenario.nextRef().genericize().equals(VACCINATION_PENTA1_REF))
                vaccines.visit(scenario);

            if (Arrays.asList(NEXT_CHILD_REF, NEXT_CHILD_NO_MOTHER_REF).contains(scenario.nextRef().genericize()))
                scenario.next();
        };
    }

    private void answerDateOfBirth(Scenario scenario, LocalDate dob) {
        // Is DoB known?
        scenario.next();
        scenario.answer("yes");
        // Year in DoB
        scenario.next();
        scenario.answer(dob.getYear());
        // Month in DoB
        scenario.next();
        scenario.answer(dob.getMonthValue());
        // Day in DoB
        scenario.next();
        scenario.answer(dob.getDayOfMonth());
    }

    private void answerAgeInMonths(Scenario scenario, int ageInMonths) {
        // Is DoB known?
        scenario.next();
        scenario.answer("no");
        // Age in months
        scenario.next();
        scenario.answer(ageInMonths);
    }

}
