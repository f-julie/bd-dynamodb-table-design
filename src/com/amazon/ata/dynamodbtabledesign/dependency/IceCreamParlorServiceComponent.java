package com.amazon.ata.dynamodbtabledesign.dependency;

import com.amazon.ata.dynamodbtabledesign.IceCreamParlorService;

import dagger.Component;

@Component
public interface IceCreamParlorServiceComponent {
    IceCreamParlorService provideIceCreamParlorService();
}
