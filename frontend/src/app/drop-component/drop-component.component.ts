import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { DisplayService } from '../services/display.service';
import { FileAndUrlModel } from '../models/FileAndUrl.model';
import { BackendRequest } from '../services/backendrequest.service';

@Component({
  selector: 'app-drop-component',
  templateUrl: './drop-component.component.html',
  styleUrls: ['./drop-component.component.css']
})
export class DropComponentComponent implements OnInit {

  @ViewChild('fileInput') fileInput: ElementRef;
  @ViewChild('droparea') droparea:ElementRef;
  greeting:string='';
  nonAllowedFiles:String[]=[];
 
  constructor(private displayService:DisplayService, private backendService:BackendRequest ) { }

  ngOnInit(): void {
    var today = new Date()
    var curHr = today.getHours()

    if (curHr < 12) {
      this.greeting = "Good Morning Writer"
    } else if (curHr < 18) {
        this.greeting = "Good Afternoon Writer"
    } else {
        this.greeting = "Good Evening Writer"
    }
  }


  onDropEvent(event)
  {
    event.preventDefault();
    event.stopPropagation();
    
    const files = event.dataTransfer.files;
    for(let file of files)
    { 
       let index = file.name.lastIndexOf(".");
       let extension = file.name.substring(index+1);
       if(extension === 'jpeg' || extension === 'JPEG' || extension === 'png' || extension === 'PNG' || extension === 'jpg' || extension === 'JPG')
       {
           this.backendService.storeIntoS3(file).subscribe(url=>{
              let fileAndUrlModel:FileAndUrlModel=  new FileAndUrlModel(file,url);
              this.displayService.displayInformation.next(fileAndUrlModel);
           });
       }
       else{
        this.nonAllowedFiles.push(file.name);
        this.displayService.alertInformation.next(file.name);
       }
    }
   
  }
  onDragEvent(droparea:HTMLDivElement,event:DragEvent)
  {
    event.preventDefault();
  }

  onDragLeave(event)
  {
    event.preventDefault();
  }
  onBrowseClick(event)
  {
    let inputElement: HTMLElement = this.fileInput.nativeElement as HTMLElement;
     inputElement.click();
  }
  uploadImage(files:FileList)
  {
    for(let i=0;i<files.length;i++)
    {
      this.backendService.storeIntoS3(files.item(i)).subscribe(url=>{
        let fileAndUrlModel:FileAndUrlModel=  new FileAndUrlModel(files.item(i),url);
        this.displayService.displayInformation.next(fileAndUrlModel);
      });
    }
  }

  onAlertDismiss(index)
  {
    this.nonAllowedFiles.splice(index,index+1);
  }
}
