package com.github.com.alexishuf.rdfit.iterator;

import com.github.com.alexishuf.rdfit.data.QuadMock1;
import com.github.com.alexishuf.rdfit.data.QuadMock2;
import com.github.com.alexishuf.rdfit.data.TripleMock1;
import com.github.com.alexishuf.rdfit.data.TripleMock2;

import javax.annotation.Nonnull;

public class Ex {
    public static final @Nonnull String NS = "http://example.org/";

    public static final @Nonnull String S1 = NS+"s/"+1;
    public static final @Nonnull String S2 = NS+"s/"+2;
    public static final @Nonnull String S3 = NS+"s/"+3;
    public static final @Nonnull String S4 = NS+"s/"+4;
    public static final @Nonnull String S5 = NS+"s/"+5;
    public static final @Nonnull String S6 = NS+"s/"+6;

    public static final @Nonnull String P1 = NS+"p/"+1;
    public static final @Nonnull String P2 = NS+"p/"+2;
    public static final @Nonnull String P3 = NS+"p/"+3;
    public static final @Nonnull String P4 = NS+"p/"+4;
    public static final @Nonnull String P5 = NS+"p/"+5;
    public static final @Nonnull String P6 = NS+"p/"+6;

    public static final @Nonnull String O1 = NS+"o/"+1;
    public static final @Nonnull String O2 = NS+"o/"+2;
    public static final @Nonnull String O3 = NS+"o/"+3;
    public static final @Nonnull String O4 = NS+"o/"+4;
    public static final @Nonnull String O5 = NS+"o/"+5;
    public static final @Nonnull String O6 = NS+"o/"+6;

    public static final @Nonnull String G1 = NS+"g/"+1;
    public static final @Nonnull String G2 = NS+"g/"+2;
    public static final @Nonnull String G3 = NS+"g/"+3;
    public static final @Nonnull String G4 = NS+"g/"+4;
    public static final @Nonnull String G5 = NS+"g/"+5;
    public static final @Nonnull String G6 = NS+"g/"+6;

    public static final @Nonnull TripleMock1 T1 = new TripleMock1(S1, P1, O1);
    public static final @Nonnull TripleMock1 T2 = new TripleMock1(S2, P2, O2);
    public static final @Nonnull TripleMock1 T3 = new TripleMock1(S3, P3, O3);
    public static final @Nonnull TripleMock1 T4 = new TripleMock1(S4, P4, O4);
    public static final @Nonnull TripleMock1 T5 = new TripleMock1(S5, P5, O5);
    public static final @Nonnull TripleMock1 T6 = new TripleMock1(S6, P6, O6);

    public static final @Nonnull TripleMock2 U1 = new TripleMock2(S1, P1, O1);
    public static final @Nonnull TripleMock2 U2 = new TripleMock2(S2, P2, O2);
    public static final @Nonnull TripleMock2 U3 = new TripleMock2(S3, P3, O3);
    public static final @Nonnull TripleMock2 U4 = new TripleMock2(S4, P4, O4);
    public static final @Nonnull TripleMock2 U5 = new TripleMock2(S5, P5, O5);
    public static final @Nonnull TripleMock2 U6 = new TripleMock2(S6, P6, O6);

    public static final @Nonnull QuadMock1 Q1 = new QuadMock1(G1, new TripleMock1(S1, P1, O1));
    public static final @Nonnull QuadMock1 Q2 = new QuadMock1(G1, new TripleMock1(S2, P2, O2));
    public static final @Nonnull QuadMock1 Q3 = new QuadMock1(G1, new TripleMock1(S3, P3, O3));
    public static final @Nonnull QuadMock1 Q4 = new QuadMock1(G1, new TripleMock1(S4, P4, O4));
    public static final @Nonnull QuadMock1 Q5 = new QuadMock1(G1, new TripleMock1(S5, P5, O5));
    public static final @Nonnull QuadMock1 Q6 = new QuadMock1(G1, new TripleMock1(S6, P6, O6));

    public static final @Nonnull QuadMock2 R1 = new QuadMock2(G1, S1, P1, O1);
    public static final @Nonnull QuadMock2 R2 = new QuadMock2(G1, S2, P2, O2);
    public static final @Nonnull QuadMock2 R3 = new QuadMock2(G1, S3, P3, O3);
    public static final @Nonnull QuadMock2 R4 = new QuadMock2(G1, S4, P4, O4);
    public static final @Nonnull QuadMock2 R5 = new QuadMock2(G1, S5, P5, O5);
    public static final @Nonnull QuadMock2 R6 = new QuadMock2(G1, S6, P6, O6);

    public static @Nonnull String uri(@Nonnull String localName) {
        return NS + localName;
    }

    public static @Nonnull TripleMock1 tm1(@Nonnull String s, @Nonnull String p, @Nonnull String o) {
        return new TripleMock1(uri(s), uri(p), uri(o));
    }
    public static @Nonnull TripleMock2 tm2(@Nonnull String s, @Nonnull String p, @Nonnull String o) {
        return new TripleMock2(uri(s), uri(p), uri(o));
    }
    public static @Nonnull QuadMock1 qm1(@Nonnull String g, @Nonnull String s, @Nonnull String p,
                                         @Nonnull String o) {
        return new QuadMock1(uri(g), new TripleMock1(uri(s), uri(p), uri(o)));
    }
    public static @Nonnull QuadMock2 qm2(@Nonnull String g, @Nonnull String s, @Nonnull String p,
                                         @Nonnull String o) {
        return new QuadMock2(uri(g), uri(s), uri(p), uri(o));
    }
}
