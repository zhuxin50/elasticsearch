/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.rankeval;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EvalQueryQualityTests extends ESTestCase {

    private static NamedWriteableRegistry namedWritableRegistry = new NamedWriteableRegistry(new RankEvalPlugin().getNamedWriteables());

    public static EvalQueryQuality randomEvalQueryQuality() {
        List<RatedDocumentKey> unknownDocs = new ArrayList<>();
        int numberOfUnknownDocs = randomInt(5);
        for (int i = 0; i < numberOfUnknownDocs; i++) {
            unknownDocs.add(RatedDocumentKeyTests.createRandomRatedDocumentKey());
        }
        int numberOfSearchHits = randomInt(5);
        List<RatedSearchHit> ratedHits = new ArrayList<>();
        for (int i = 0; i < numberOfSearchHits; i++) {
            ratedHits.add(RatedSearchHitTests.randomRatedSearchHit());
        }
        EvalQueryQuality evalQueryQuality = new EvalQueryQuality(randomAsciiOfLength(10), randomDoubleBetween(0.0, 1.0, true), unknownDocs);
        if (randomBoolean()) {
            // TODO randomize this
            evalQueryQuality.addMetricDetails(new PrecisionAtN.Breakdown(1, 5));
        }
        evalQueryQuality.addHitsAndRatings(ratedHits);
        return evalQueryQuality;
    }

    public void testSerialization() throws IOException {
        EvalQueryQuality original = randomEvalQueryQuality();
        EvalQueryQuality deserialized = RankEvalTestHelper.copy(original, EvalQueryQuality::new, namedWritableRegistry);
        assertEquals(deserialized, original);
        assertEquals(deserialized.hashCode(), original.hashCode());
        assertNotSame(deserialized, original);
    }

    public void testEqualsAndHash() throws IOException {
        EvalQueryQuality testItem = randomEvalQueryQuality();
        RankEvalTestHelper.testHashCodeAndEquals(testItem, mutateTestItem(testItem),
                RankEvalTestHelper.copy(testItem, EvalQueryQuality::new, namedWritableRegistry));
    }

    private static EvalQueryQuality mutateTestItem(EvalQueryQuality original) {
        String id = original.getId();
        double qualityLevel = original.getQualityLevel();
        List<RatedDocumentKey> unknownDocs = new ArrayList<>(original.getUnknownDocs());
        List<RatedSearchHit> ratedHits = new ArrayList<>(original.getHitsAndRatings());
        MetricDetails breakdown = original.getMetricDetails();
        switch (randomIntBetween(0, 3)) {
        case 0:
            id = id + "_";
            break;
        case 1:
            qualityLevel = qualityLevel + 0.1;
            break;
        case 2:
            unknownDocs.add(RatedDocumentKeyTests.createRandomRatedDocumentKey());
            break;
        case 3:
            if (breakdown == null) {
                breakdown = new PrecisionAtN.Breakdown(1, 5);
            } else {
                breakdown = null;
            }
            break;
        case 4:
            ratedHits.add(RatedSearchHitTests.randomRatedSearchHit());
            break;
        default:
            throw new IllegalStateException("The test should only allow five parameters mutated");
        }
        EvalQueryQuality evalQueryQuality = new EvalQueryQuality(id, qualityLevel, unknownDocs);
        evalQueryQuality.addMetricDetails(breakdown);
        evalQueryQuality.addHitsAndRatings(ratedHits);
        return evalQueryQuality;
    }
}
