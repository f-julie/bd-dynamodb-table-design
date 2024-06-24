package com.amazon.ata.dynamodbtabledesign.dependency;

import com.amazon.ata.dynamodbtabledesign.IceCreamMaker;
import com.amazon.ata.dynamodbtabledesign.IceCreamParlorService;
import com.amazon.ata.dynamodbtabledesign.dao.CartonDao;
import com.amazon.ata.dynamodbtabledesign.dao.RecipeDao;
import javax.annotation.processing.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerIceCreamParlorServiceComponent implements IceCreamParlorServiceComponent {
  private DaggerIceCreamParlorServiceComponent(Builder builder) {}

  public static Builder builder() {
    return new Builder();
  }

  public static IceCreamParlorServiceComponent create() {
    return new Builder().build();
  }

  @Override
  public IceCreamParlorService provideIceCreamParlorService() {
    return new IceCreamParlorService(new RecipeDao(), new CartonDao(), new IceCreamMaker());
  }

  public static final class Builder {
    private Builder() {}

    public IceCreamParlorServiceComponent build() {
      return new DaggerIceCreamParlorServiceComponent(this);
    }
  }
}
