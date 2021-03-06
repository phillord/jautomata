/*
 * ______________________________________________________________________________
 * 
 * Copyright 2005 Arnaud Bailly - NORSYS/LIFL
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * (2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * (3) The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on 31 mars 2005
 * 
 */
package rationals.ioautomata;

import rationals.*;

import java.util.*;

/**
 * An IO Automata is a special kind of Automaton where the alphabet is
 * partitionned in three disjoint sets : input, output and internal actions.
 * <em>Note</em> : the various alphabet accessors methods should be used with
 * care. They store labels of transitions without any information about their
 * status, whihc means intersection of the sets may not be empty.
 * <p>
 * 
 * @author nono
 * @version $Id: IOAutomaton.java 13 2007-06-01 16:11:03Z oqube $
 */
public class IOAutomaton<I extends IOTransition, T extends Builder<I, T>> extends
		Automaton<I, T> {

	private Set input = new HashSet();

	private Set output = new HashSet();

	private Set internal = new HashSet();

	public IOAutomaton() {
	}

	public IOAutomaton(T t) {
		setBuilder(t);
	}

	/**
	 * Construct an IOAutomaton from a standard automaton.
	 * <p>
	 * This constructor assumes automaton is labelled with IOLetter objects.
	 * 
	 * @param automaton
	 */
	public IOAutomaton(Automaton automaton) {
		Map<State, State> sm = new HashMap<State, State>();
		for (Iterator i = automaton.states().iterator(); i.hasNext();) {
			State st = (State) i.next();
			State ns = addState(st.isInitial(), st.isTerminal());
			sm.put(st, ns);
		}
		/* transitions */
		for (Iterator i = automaton.delta().iterator(); i.hasNext();) {
			Transition tr = (Transition) i.next();
			IOTransition.IOLetter lt = (IOTransition.IOLetter) tr.label();
			try {
				addTransition(new IOTransition(sm.get(tr.start()), lt.label, sm
						.get(tr.end()), lt.type));
			} catch (NoSuchStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Construct an IOAutomaton from a standard automaton using an alphabet Map.
	 * <p>
	 * This constructor allows transformation of a standard automaton to an
	 * IOAutomaton using a map that transforms source automaton's letters to
	 * IOLetter objects.
	 * 
	 * @param a
	 *            the source automaton
	 * @param m
	 *            the map. Values must be IOLetter instances.
	 * @see rationals.ioautomata.IOTransition
	 */
	public IOAutomaton(Automaton a, Map m) {
		Map<State, State> sm = new HashMap<State, State>();
		for (Iterator i = a.states().iterator(); i.hasNext();) {
			State st = (State) i.next();
			State ns = addState(st.isInitial(), st.isTerminal());
			sm.put(st, ns);
		}
		/* transitions */
		for (Iterator i = a.delta().iterator(); i.hasNext();) {
			Transition tr = (Transition) i.next();
			IOTransition.IOLetter lt = (IOTransition.IOLetter) m
					.get(tr.label());
			try {
				addTransition(new IOTransition(sm.get(tr.start()), lt.label, sm
						.get(tr.end()), lt.type));
			} catch (NoSuchStateException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method expects an {@see rationals.ioautomaton.IOTransition}.
	 * 
	 * @see rationals.Automaton#addTransition(rationals.Transition)
	 */
	public void addTransition(Transition transition)
			throws NoSuchStateException {
		IOTransition iot = (IOTransition) transition;
		switch (iot.getType()) {
		case INPUT:
			input.add(iot.label());
			break;
		case OUTPUT:
			output.add(iot.label());
			break;
		case INTERNAL:
			internal.add(iot.label());
			break;
		}
		super.addTransition(iot);
	}

	/**
	 * @return Returns the input.
	 */
	public Set<Object> getInput() {
		return new HashSet<Object>(input);
	}

	/**
	 * @return Returns the internal.
	 */
	public Set<Object> getInternal() {
		return new HashSet<Object>(internal);
	}

	/**
	 * @return Returns the output.
	 */
	public Set<Object> getOutput() {
		return new HashSet<Object>(output);
	}

	/**
	 * This methods completes the transitions in this IOAutomaton w.r.t. to the
	 * alphabet of another automaton. That is, for each state <code>q</code> and
	 * for each letter <code>l</code> in <code>ioa</code>'s alphabet, if there
	 * is no transition labelled with <code>l</code> starting from
	 * <code>q</code>, it adds a transition <code>(q,l,q)</code> to this
	 * automaton.
	 * 
	 * @param ioa
	 */
	public void complete(IOAutomaton ioa) {
		Set alph = new HashSet();
		for (Iterator it = states().iterator(); it.hasNext();) {
			State q = (State) it.next();
			alph.addAll(ioa.alphabet());
			for (Iterator i2 = delta(q).iterator(); i2.hasNext();) {
				Transition tr = (Transition) i2.next();
				alph.remove(tr.label());
			}
			for (Iterator i2 = alph.iterator(); i2.hasNext();) {
				try {
					addTransition(new IOTransition(q, i2.next(), q));
				} catch (NoSuchStateException e) {
				}
			}
			alph.clear();
		}
	}
}