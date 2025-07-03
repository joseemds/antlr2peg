// Generated from antlr/dot/dot.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link dotParser}.
 */
public interface dotListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link dotParser#graph}.
	 * @param ctx the parse tree
	 */
	void enterGraph(dotParser.GraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#graph}.
	 * @param ctx the parse tree
	 */
	void exitGraph(dotParser.GraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(dotParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(dotParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(dotParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(dotParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#attr_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAttr_stmt(dotParser.Attr_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#attr_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAttr_stmt(dotParser.Attr_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#attr_list}.
	 * @param ctx the parse tree
	 */
	void enterAttr_list(dotParser.Attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#attr_list}.
	 * @param ctx the parse tree
	 */
	void exitAttr_list(dotParser.Attr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#a_list}.
	 * @param ctx the parse tree
	 */
	void enterA_list(dotParser.A_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#a_list}.
	 * @param ctx the parse tree
	 */
	void exitA_list(dotParser.A_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#edge_stmt}.
	 * @param ctx the parse tree
	 */
	void enterEdge_stmt(dotParser.Edge_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#edge_stmt}.
	 * @param ctx the parse tree
	 */
	void exitEdge_stmt(dotParser.Edge_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#edgeRHS}.
	 * @param ctx the parse tree
	 */
	void enterEdgeRHS(dotParser.EdgeRHSContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#edgeRHS}.
	 * @param ctx the parse tree
	 */
	void exitEdgeRHS(dotParser.EdgeRHSContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void enterEdgeop(dotParser.EdgeopContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#edgeop}.
	 * @param ctx the parse tree
	 */
	void exitEdgeop(dotParser.EdgeopContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#node_stmt}.
	 * @param ctx the parse tree
	 */
	void enterNode_stmt(dotParser.Node_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#node_stmt}.
	 * @param ctx the parse tree
	 */
	void exitNode_stmt(dotParser.Node_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#node_id}.
	 * @param ctx the parse tree
	 */
	void enterNode_id(dotParser.Node_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#node_id}.
	 * @param ctx the parse tree
	 */
	void exitNode_id(dotParser.Node_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(dotParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(dotParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#subgraph}.
	 * @param ctx the parse tree
	 */
	void enterSubgraph(dotParser.SubgraphContext ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#subgraph}.
	 * @param ctx the parse tree
	 */
	void exitSubgraph(dotParser.SubgraphContext ctx);
	/**
	 * Enter a parse tree produced by {@link dotParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(dotParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link dotParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(dotParser.Id_Context ctx);
}