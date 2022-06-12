package furkanodev;

import java.util.ArrayList;
import java.util.List;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.FOLFCAsk;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.inference.InferenceResult;

public class App {
	
	public static FOLDomain createDomain() {
		FOLDomain domain = new FOLDomain();
		
		domain.addPredicate("person");
		domain.addPredicate("man");
		domain.addPredicate("woman");
		
		domain.addConstant("George");
		domain.addConstant("John");
		domain.addConstant("Robert");
		domain.addConstant("Barbara");
		domain.addConstant("Christine");
		domain.addConstant("Yolanda");
		
		domain.addPredicate("location");
		
		domain.addConstant("bathroom");
		domain.addConstant("dining");
		domain.addConstant("kitchen");
		domain.addConstant("living");
		domain.addConstant("pantry");
		domain.addConstant("study");
		
		domain.addPredicate("weapon");
		
		domain.addConstant("bag");
		domain.addConstant("firearm");
		domain.addConstant("gas");
		domain.addConstant("knife");
		domain.addConstant("poison");
		domain.addConstant("rope");
		
		domain.addPredicate("in");
		domain.addPredicate("has");
		
		domain.addPredicate("murderer");
		
		return domain;
	}
	
	public static FOLKnowledgeBase createKnowledgeBase(InferenceProcedure infp) {
		FOLKnowledgeBase kb = new FOLKnowledgeBase(createDomain(), infp);
		
		kb.tell("(man(x) => person(x))"); // MUST wrap up the entire statement with paranthesis!
		kb.tell("(woman(x) => person(x))");
		kb.tell("man(George)");
		kb.tell("man(John)");
		kb.tell("man(Robert)");
		kb.tell("woman(Barbara)");
		kb.tell("woman(Christine)");
		kb.tell("woman(Yolanda)");
		
		kb.tell("(murderer(x) => person(x))");
		
		kb.tell("location(bathroom)");
		kb.tell("location(dining)");
		kb.tell("location(kitchen)");
		kb.tell("location(living)");
		kb.tell("location(pantry)");
		kb.tell("location(study)");
		
		kb.tell("weapon(bag)");
		kb.tell("weapon(firearm)");
		kb.tell("weapon(gas)");
		kb.tell("weapon(knife)");
		kb.tell("weapon(poison)");
		kb.tell("weapon(rope)");
		
		kb.tell("(in(x,y) => ((person(x) OR weapon(x)) AND location(y)))");

		// there is at least one person&weapon in each location.
		kb.tell("FORALL y (location(y) => EXISTS x in(x,y))");
		// there is EXACTLY ONE (i.e., no more than one) person&weapon in each location.
		// i.o.w: if there is already a person/weapon y in location z, then x cannot be in z.
		kb.tell("FORALL x (FORALL z (FORALL y (NOT(x = y) AND in(y,z)) => NOT(in(x,z))))");

		// if person x and weapon y are in z, then x has y.
		kb.tell("FORALL x (FORALL y (FORALL z (person(x) AND weapon(y) AND in(x,z) AND in(y,z)) => has(x,y)))");
		// if x has y, that means there is location z where both x and y are in.
		kb.tell("FORALL x (FORALL y (has(x,y) => EXISTS z (in(x,z) AND in(y,z))))");

		// CLUE 1
		kb.tell("(in(x,kitchen) => man(x))");
		kb.tell("(in(x,kitchen) => NOT(has(x,rope)))");
		kb.tell("(in(x,kitchen) => NOT(has(x,knife)))");
		kb.tell("(in(x,kitchen) => NOT(has(x,bag)))");
		kb.tell("(in(x,kitchen) => NOT(has(x,firearm)))");
		
		// CLUE 2
		kb.tell("((in(Barbara,bathroom) AND in(Yolanda,study)) OR (in(Barbara,study) AND in(Yolanda,bathroom)))");
		
		// CLUE 3
		kb.tell("(has(x,bag) => NOT(x = Barbara))");
		kb.tell("(has(x,bag) => NOT(x = George))");
		kb.tell("(has(x,bag) => NOT(in(x,bathroom)))");
		kb.tell("(has(x,bag) => NOT(in(x,dining)))");

		// CLUE 4
		kb.tell("(has(x,rope) => (woman(x) AND in(x,study)))");
		
		// CLUE 5
		kb.tell("(in(John,living) OR in(George,living))");
		
		// CLUE 6
		kb.tell("NOT(in(knife,dining))");

		// CLUE 7
		kb.tell("NOT(in(Yolanda,study))");
		kb.tell("NOT(in(Yolanda,pantry))");

		// CLUE 8
		kb.tell("has(George,firearm)");
		
		// --------------------------------------------------------------
		kb.tell("in(gas,pantry)");
		kb.tell("(has(x,gas)  => murderer(x))");
		
		return kb;
	}

	public static void main(String[] args) {
		FOLKnowledgeBase kkb = createKnowledgeBase(new FOLFCAsk());
		List<Term> terms = new ArrayList<>();
//		terms.add(new Constant("Yolanda"));
		terms.add(new Variable("x"));
//		terms.add(new Constant("bathroom"));
		Predicate query = new Predicate("murderer", terms);
		InferenceResult answer = kkb.ask(query);
//		System.out.println(answer.isTrue());
		if (answer.isTrue())
			System.out.println(answer.getProofs().get(0).getAnswerBindings().get(new Variable("x")).toString());
		else
			System.out.println("false");
	}
}

// https://github.com/xmonader/prolog-rands/blob/master/crime.pl
