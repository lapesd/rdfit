package com.github.lapesd.rdfit.iterator;

import com.github.lapesd.rdfit.components.converters.impl.DefaultConversionManager;
import com.github.lapesd.rdfit.data.MockFactory;
import com.github.lapesd.rdfit.data.QuadMock;
import com.github.lapesd.rdfit.data.SplitMockQuad;
import com.github.lapesd.rdfit.data.TripleMock;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.github.lapesd.rdfit.data.MockHelpers.*;

public class ListenerRDFItTest extends RDFItTestBase {
    private static final @Nonnull Object TEST_SOURCE = new Object() {
        @Override public String toString() {
            return "CallbackRDFItTest.TEST_SOURCE";
        }
    };

    @Override protected @Nonnull <T> RDFIt<T>
    createIt(@Nonnull Class<T> valueClass, @Nonnull IterationElement itElement,
             @Nonnull List<?> data) {
        DefaultConversionManager conMgr = new DefaultConversionManager();
        Class<?> quadType = isAnyMock(valueClass) || isQuadType(valueClass) ? valueClass : null;
        ListenerRDFIt<T> it;
        it = new ListenerRDFIt<>(TEST_SOURCE, valueClass, itElement, quadLifter(quadType), conMgr);
        feedMocksToCallback(it.getListener(), TEST_SOURCE, data).finish();
        return it;
    }

    @Override
    protected @Nonnull List<?>
    adjustExpected(@Nonnull List<?> data, @Nonnull Class<?> valueClass,
                   @Nonnull IterationElement itEl) {
        ArrayList<Object> list = new ArrayList<>();
        for (Object o : data) {
            if (valueClass.isInstance(o)) {
                list.add(o);
            } else if (itEl.isTriple() && o instanceof QuadMock) {
                list.add(SplitMockQuad.split(o).getTriple());
            } else if (itEl.isQuad() && o instanceof TripleMock) {
                list.add(MockFactory.triple2quad(valueClass, "", o));
            } else {
                list.add(o);
            }
        }
        return list;
    }
}