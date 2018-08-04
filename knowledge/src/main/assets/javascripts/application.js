import 'bootstrap';

import Turbolinks from "turbolinks";

import { Application } from "stimulus"
import IndexController from "./controllers/index_controller"

const application = Application.start()
application.register("index", IndexController)

Turbolinks.start();
