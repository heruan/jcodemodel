/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.helger.jcodemodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents X&lt;Y>. TODO: consider separating the decl and the use.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JNarrowedClass extends AbstractJClass
{
  /**
   * A generic class with type parameters.
   */
  private final AbstractJClass _basis;
  /**
   * Arguments to those parameters.
   */
  private final List <AbstractJClass> _args;

  protected JNarrowedClass (@Nonnull final AbstractJClass basis, final AbstractJClass arg)
  {
    this (basis, Collections.singletonList (arg));
  }

  protected JNarrowedClass (@Nonnull final AbstractJClass basis, final List <AbstractJClass> args)
  {
    super (basis.owner ());
    this._basis = basis;
    assert !(basis instanceof JNarrowedClass);
    this._args = args;
  }

  @Nonnull
  public AbstractJClass basis ()
  {
    return _basis;
  }

  @Override
  public AbstractJClass narrow (final AbstractJClass clazz)
  {
    final List <AbstractJClass> newArgs = new ArrayList <AbstractJClass> (_args);
    newArgs.add (clazz);
    return new JNarrowedClass (_basis, newArgs);
  }

  @Override
  public AbstractJClass narrow (final AbstractJClass... clazz)
  {
    final List <AbstractJClass> newArgs = new ArrayList <AbstractJClass> (_args);
    newArgs.addAll (Arrays.asList (clazz));
    return new JNarrowedClass (_basis, newArgs);
  }

  @Override
  public String name ()
  {
    final StringBuilder buf = new StringBuilder ();
    buf.append (_basis.name ()).append ('<');
    boolean first = true;
    for (final AbstractJClass c : _args)
    {
      if (first)
        first = false;
      else
        buf.append (',');
      buf.append (c.name ());
    }
    buf.append ('>');
    return buf.toString ();
  }

  @Override
  public String fullName ()
  {
    final StringBuilder buf = new StringBuilder ();
    buf.append (_basis.fullName ());
    buf.append ('<');
    boolean first = true;
    for (final AbstractJClass c : _args)
    {
      if (first)
        first = false;
      else
        buf.append (',');
      buf.append (c.fullName ());
    }
    buf.append ('>');
    return buf.toString ();
  }

  @Override
  public String binaryName ()
  {
    final StringBuilder buf = new StringBuilder ();
    buf.append (_basis.binaryName ());
    buf.append ('<');
    boolean first = true;
    for (final AbstractJClass c : _args)
    {
      if (first)
        first = false;
      else
        buf.append (',');
      buf.append (c.binaryName ());
    }
    buf.append ('>');
    return buf.toString ();
  }

  @Override
  public void generate (final JFormatter f)
  {
    f.type (_basis).print ('<').generable (_args).print (JFormatter.CLOSE_TYPE_ARGS);
  }

  @Override
  void printLink (final JFormatter f)
  {
    _basis.printLink (f);
    f.print ("{@code <}");
    boolean first = true;
    for (final AbstractJClass c : _args)
    {
      if (first)
        first = false;
      else
        f.print (',');
      c.printLink (f);
    }
    f.print ("{@code >}");
  }

  @Override
  public JPackage _package ()
  {
    return _basis._package ();
  }

  @Override
  public AbstractJClass _extends ()
  {
    final AbstractJClass base = _basis._extends ();
    if (base == null)
      return base;
    return base.substituteParams (_basis.typeParams (), _args);
  }

  @Override
  public Iterator <AbstractJClass> _implements ()
  {
    return new Iterator <AbstractJClass> ()
    {
      private final Iterator <AbstractJClass> core = _basis._implements ();

      public void remove ()
      {
        core.remove ();
      }

      public AbstractJClass next ()
      {
        return core.next ().substituteParams (_basis.typeParams (), _args);
      }

      public boolean hasNext ()
      {
        return core.hasNext ();
      }
    };
  }

  @Override
  public AbstractJClass erasure ()
  {
    return _basis;
  }

  @Override
  public boolean isInterface ()
  {
    return _basis.isInterface ();
  }

  @Override
  public boolean isAbstract ()
  {
    return _basis.isAbstract ();
  }

  @Override
  public boolean isArray ()
  {
    return false;
  }

  //
  // Equality is based on value
  //

  @Override
  public boolean equals (final Object obj)
  {
    if (!(obj instanceof JNarrowedClass))
      return false;
    return fullName ().equals (((AbstractJClass) obj).fullName ());
  }

  @Override
  public int hashCode ()
  {
    return fullName ().hashCode ();
  }

  @Override
  protected AbstractJClass substituteParams (final JTypeVar [] variables, final List <AbstractJClass> bindings)
  {
    final AbstractJClass b = _basis.substituteParams (variables, bindings);
    boolean different = b != _basis;

    final List <AbstractJClass> clazz = new ArrayList <AbstractJClass> (_args.size ());
    for (int i = 0; i < clazz.size (); i++)
    {
      final AbstractJClass c = _args.get (i).substituteParams (variables, bindings);
      clazz.set (i, c);
      different |= c != _args.get (i);
    }

    if (different)
      return new JNarrowedClass (b, clazz);
    return this;
  }

  @Override
  public List <AbstractJClass> getTypeParameters ()
  {
    return _args;
  }
}
