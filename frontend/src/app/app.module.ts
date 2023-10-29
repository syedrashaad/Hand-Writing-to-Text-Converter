import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { DropComponentComponent } from './drop-component/drop-component.component';
import { DisplayImageComponent } from './display-image/display-image.component';
import { DisplayService } from './services/display.service';
import { BackendRequest } from './services/backendrequest.service';

@NgModule({
  declarations: [
    AppComponent,
    DropComponentComponent,
    DisplayImageComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [DisplayService, BackendRequest],
  bootstrap: [AppComponent]
})
export class AppModule { }
