/*
 * Copyright 2018 Marten Gajda <marten@dmfs.org>
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.rfc5545.hamcrest;

import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.generatable.Generatable;
import org.dmfs.jems.hamcrest.matchers.GeneratableMatcher;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;


/**
 * A {@link Matcher} which checks the first instances generated by a {@link RecurrenceRule}.
 *
 * @author Marten Gajda
 */
public final class GeneratorMatcher extends TypeSafeDiagnosingMatcher<RecurrenceRule>
{
    private final DateTime mStart;
    private final Matcher<Generatable<DateTime>> mInstanceMatcher;


    public GeneratorMatcher(DateTime start, Matcher<Generatable<DateTime>> instancematcher)
    {
        mStart = start;
        mInstanceMatcher = instancematcher;
    }


    public static Matcher<RecurrenceRule> generates(String start, String... instances)
    {
        return generates(DateTime.parse(start), new Mapped<>(DateTime::parse, new Seq<>(instances)));
    }


    public static Matcher<RecurrenceRule> generates(DateTime start, Iterable<DateTime> instances)
    {
        return new GeneratorMatcher(start, GeneratableMatcher.startsWith(new Mapped<>(Matchers::is, instances)));
    }


    public static Matcher<RecurrenceRule> generates(DateTime start, Matcher<Generatable<DateTime>> instancematcher)
    {
        return new GeneratorMatcher(start, instancematcher);
    }


    @Override
    protected boolean matchesSafely(RecurrenceRule item, Description mismatchDescription)
    {
        // a Generatable of the recurrence instances
        Generatable<DateTime> generatable = () -> item.iterator(mStart)::nextDateTime;

        if (!mInstanceMatcher.matches(generatable))
        {
            mInstanceMatcher.describeMismatch(generatable, mismatchDescription);
            return false;
        }
        return true;
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("generated events ");
        mInstanceMatcher.describeTo(description);
    }
}