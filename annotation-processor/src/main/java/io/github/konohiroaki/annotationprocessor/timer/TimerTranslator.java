package io.github.konohiroaki.annotationprocessor.timer;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.sun.tools.javac.tree.JCTree.JCAnnotation;
import static com.sun.tools.javac.tree.JCTree.JCBlock;
import static com.sun.tools.javac.tree.JCTree.JCExpression;
import static com.sun.tools.javac.tree.JCTree.JCLiteral;
import static com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import static com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import static com.sun.tools.javac.tree.JCTree.JCModifiers;
import static com.sun.tools.javac.tree.JCTree.JCStatement;
import static com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import static com.sun.tools.javac.tree.JCTree.Tag;

public class TimerTranslator extends TreeTranslator {

    private final TreeMaker maker;
    private final JavacElements elements;

    private static final String TIMER = "timer";

    TimerTranslator(Context context) {
        this.maker = TreeMaker.instance(context);
        this.elements = JavacElements.instance(context);
    }

    @Override
    public void visitMethodDef(JCMethodDecl methodDecl) {
        super.visitMethodDef(methodDecl);

        boolean hasTargetAnnotation = methodDecl.mods.annotations.stream()
            .anyMatch(e -> e.getAnnotationType().type.toString().equals(Timer.class.getName()));
        if (hasTargetAnnotation) {
            result = createMethodDeclaration(methodDecl);
        }
    }

    private JCMethodDecl createMethodDeclaration(JCMethodDecl methodDecl) {
        JCAnnotation annotation = methodDecl.mods.annotations.stream()
            .filter(e -> e.getAnnotationType().type.toString().equals(Timer.class.getName()))
            .findFirst().get(/*always present*/);
        List args = (List) annotation.attribute.values.get(0).snd.getValue();

        JCBlock body = createBody(methodDecl.body, args);

        return maker.MethodDef(methodDecl.mods, methodDecl.name,
                               methodDecl.restype, methodDecl.typarams,
                               methodDecl.params, methodDecl.thrown,
                               body, methodDecl.defaultValue);
    }

    private JCBlock createBody(JCBlock oldBody, List args) {
        int start = (int) ((Attribute.Constant) args.get(0)).value,
            end = (int) ((Attribute.Constant) args.get(1)).value;

        ListBuffer<JCTree.JCStatement> list = new ListBuffer<>();
        IntStream.range(0, oldBody.stats.size()).forEach(idx -> {
            if (idx == start) {
                list.add(println("Timer Starting..."));
                list.add(startTimer());
            }
            list.add(oldBody.stats.get(idx));
            if (idx == end) {
                list.add(stopTimer());
                list.add(showDuration(new int[]{start, end}));
            }
        });

        return maker.Block(0 /* com.sun.tools.javac.code.Flags */, list.toList());
    }

    private JCVariableDecl startTimer() {
        JCModifiers mods = maker.Modifiers(0);
        Name name = elements.getName(TIMER);
        JCExpression type = maker.QualIdent(getClassSymbol(StopWatch.class));
        JCMethodInvocation invocation = maker.App(maker.QualIdent(findMember(StopWatch.class, "createStarted")));

        return maker.VarDef(mods, name, type, invocation);
    }

    private JCStatement stopTimer() {
        Symbol symbol = findMember(StopWatch.class, "stop");
        JCMethodInvocation stopTimer = maker.App(maker.Select(maker.Ident(elements.getName(TIMER)), symbol));

        return maker.Exec(stopTimer);
    }

    private JCStatement showDuration(int[] step) {
        Symbol symbol = findMember(StopWatch.class, "getTime");
        JCExpression method = maker.Select(maker.Ident(elements.getName(TIMER)), symbol);
        List<JCExpression> args = List.of(maker.QualIdent(findMember(TimeUnit.class, "MILLISECONDS")));
        JCMethodInvocation duration = maker.App(method, args);

        return println(prettifyDuration(duration, step));
    }

    private JCExpression prettifyDuration(JCExpression duration, int[] step) {
        JCLiteral leading = maker.Literal("Duration from step [" + step[0] + "] to [" + step[1] + "] was ");
        JCLiteral trailing = maker.Literal("ms.");

        return maker.Binary(Tag.PLUS, maker.Binary(Tag.PLUS, leading, duration), trailing);
    }

    private JCStatement println(JCExpression exp) {
        JCExpression method = maker.Select(
            maker.Select(maker.QualIdent(getClassSymbol(System.class)),
                         elements.getName("out")),
            elements.getName("println"));

        JCMethodInvocation invocation = maker.Apply(List.nil(), method, List.of(exp));
        return maker.Exec(invocation);
    }

    private JCStatement println(String str) {
        return println(maker.Literal(str));
    }

    private Symbol.ClassSymbol getClassSymbol(Class<?> clazz) {
        return elements.getTypeElement(clazz.getName());
    }

    private Symbol findMember(Class<?> clazz, String name) {
        return getClassSymbol(clazz).getEnclosedElements().stream()
            .filter(e -> e.getSimpleName().toString().equals(name))
            .findFirst().orElse(null);
    }
}
