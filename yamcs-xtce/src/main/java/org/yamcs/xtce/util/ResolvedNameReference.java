package org.yamcs.xtce.util;

import java.util.concurrent.CompletableFuture;

import org.yamcs.xtce.NameDescription;

/**
 * Reference that is resolved since the beginning - it calls any action immediately.
 * <p>
 * The reason for this class is that we do not want duplicate code paths in the SpreadSheet Loader (or other database
 * loader)
 * <ul>
 * <li>one path for the case when the named entities are found in the current space system</li>
 * <li>one path for the case when they are not found and will be resolved later.</li>
 * </ul>
 * 
 */
public class ResolvedNameReference extends AbstractNameReference {
    final NameDescription nd;
    public ResolvedNameReference(String ref, Type type, NameDescription nd) {
        super(ref, type);
        this.nd = nd;
    }
    @Override
    public boolean tryResolve(NameDescription nd) {
        return true;
    }
    
    @Override
    public NameReference addResolvedAction(ResolvedAction action) {
        action.resolved(nd);
        return this;
    }
    @Override
    public boolean isResolved() {
        return true;
    }
    
    @Override
    public CompletableFuture<NameDescription> getResolvedFuture() {
        return CompletableFuture.completedFuture(nd);
    }
}
