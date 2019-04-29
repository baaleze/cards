import { Pipe, PipeTransform } from '@angular/core';
import { User } from './model/user';

@Pipe({
  name: 'user'
})
export class UserPipe implements PipeTransform {

  transform(value: User): string {
    return value ? `${value.name}@${value.ip} [${value.id}]` : 'NULL USER';
  }

}
